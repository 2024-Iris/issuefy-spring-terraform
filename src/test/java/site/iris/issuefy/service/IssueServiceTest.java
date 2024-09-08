package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.IssueStar;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.CommentsDto;
import site.iris.issuefy.model.dto.IssueDetailDto;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.model.dto.IssueWithPagedDto;
import site.iris.issuefy.model.dto.IssueWithStarDto;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.IssueStarRepository;
import site.iris.issuefy.response.IssueDetailAndCommentsResponse;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;
import site.iris.issuefy.response.StarRepositoryIssuesResponse;

class IssueServiceTest {

	@Mock
	private WebClient webClient;
	@Mock
	private GithubTokenService githubTokenService;
	@Mock
	private IssueRepository issueRepository;
	@Mock
	private RepositoryService repositoryService;
	@Mock
	private LabelService labelService;
	@Mock
	private IssueLabelRepository issueLabelRepository;
	@Mock
	private UserService userService;
	@Mock
	private IssueStarRepository issueStarRepository;

	@InjectMocks
	private IssueService issueService;

	@Mock
	private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
	@Mock
	private WebClient.RequestHeadersSpec requestHeadersSpec;
	@Mock
	private WebClient.ResponseSpec responseSpec;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		when(webClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
	}

	@Test
	@DisplayName("리포지토리 이슈를 가져오고 동기화한다.")
	void getRepositoryIssues_shouldSynchronizeAndReturnIssues() {
		// Given
		String orgName = "testOrg";
		String repoName = "testRepo";
		String githubId = "testUser";
		Repository repository = new Repository(null, repoName, 123L, LocalDateTime.now());
		User user = new User(githubId, "test@email.com");

		when(repositoryService.findRepositoryByName(repoName)).thenReturn(repository);
		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(issueRepository.existsById(repository.getId())).thenReturn(false);

		IssueDto mockIssueDto = IssueDto.of(1L, "Test Issue", false, "open", LocalDateTime.now().minusDays(1),
			LocalDateTime.now(), null, new ArrayList<>());

		when(responseSpec.bodyToFlux(IssueDto.class)).thenReturn(Flux.just(mockIssueDto));

		Issue mockIssue = Issue.of(repository, "Test Issue", false, "open", LocalDateTime.now().minusDays(1),
			LocalDateTime.now(), null, 1L, new ArrayList<>());
		IssueWithPagedDto mockIssueWithStatus = new IssueWithPagedDto(mockIssue, false);
		Page<IssueWithPagedDto> mockPage = new PageImpl<>(
			List.of(mockIssueWithStatus),
			PageRequest.of(0, 10),
			1
		);
		when(issueRepository.findIssuesWithPaged(eq(repository.getId()), eq(user.getId()), any(Pageable.class)))
			.thenReturn(mockPage);

		// When
		PagedRepositoryIssuesResponse response = issueService.getRepositoryIssues(orgName, repoName, githubId, 0, 10,
			"created", "desc");

		// Then
		assertNotNull(response);
		verify(issueRepository, times(1)).saveAll(anyList());
		verify(labelService, times(1)).saveAllLabels(anyList());
	}

	@Test
	@DisplayName("새 이슈를 추가한다.")
	void synchronizeRepositoryIssues_whenNoExistingIssues_shouldAddNewIssues() {
		// Given
		Repository repository = new Repository(null, "testRepo", 123L, LocalDateTime.now());
		when(issueRepository.existsById(repository.getId())).thenReturn(false);

		IssueDto mockIssueDto = IssueDto.of(1L, "Test Issue", false, "open", LocalDateTime.now().minusDays(1),
			LocalDateTime.now(), null, new ArrayList<>());

		when(responseSpec.bodyToFlux(IssueDto.class)).thenReturn(Flux.just(mockIssueDto));

		// When
		issueService.synchronizeRepositoryIssues(repository, "testOrg", "testRepo", "testUser");

		// Then
		verify(issueRepository, times(1)).saveAll(anyList());
		verify(labelService, times(1)).saveAllLabels(anyList());
	}

	@Test
	@DisplayName("GitHub에서 가져온 이슈가 로컬 이슈보다 최신일 때 업데이트한다.")
	void updateExistingIssues_whenGithubIssueIsNewerAndDifferent_shouldUpdate() {
		// Given
		Org org = new Org("testOrg", 1L);
		Repository mockedRepository = mock(Repository.class);
		when(mockedRepository.getId()).thenReturn(1L);
		when(mockedRepository.getOrg()).thenReturn(org);
		when(mockedRepository.getName()).thenReturn("testRepo");
		when(mockedRepository.getGhRepoId()).thenReturn(123L);
		when(mockedRepository.getLatestUpdateAt()).thenReturn(LocalDateTime.now());

		LocalDateTime oldDate = LocalDateTime.now().minusDays(2);
		LocalDateTime newDate = LocalDateTime.now().minusDays(1);

		Issue localIssue = Issue.of(mockedRepository, "Old Issue", false, "open", oldDate,
			oldDate, null, 1L, new ArrayList<>());
		when(issueRepository.findFirstByRepositoryIdOrderByUpdatedAtDesc(mockedRepository.getId()))
			.thenReturn(Optional.of(localIssue));

		IssueDto newerGithubIssue = IssueDto.of(2L, "Updated Issue", false, "open", newDate,
			newDate, null, new ArrayList<>());

		when(githubTokenService.findAccessToken(anyString())).thenReturn("testToken");
		when(responseSpec.bodyToFlux(IssueDto.class)).thenReturn(Flux.just(newerGithubIssue));

		// When
		issueService.updateExistingIssues("testOrg", "testRepo", "testUser", mockedRepository);

		// Then
		verify(issueRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("페이지네이션된 이슈 응답을 생성한다.")
	void getPagedRepositoryIssuesResponse_shouldReturnPagedResponse() {
		// Given
		Repository repository = new Repository(null, "testRepo", 123L, LocalDateTime.now());
		User user = new User("testUser", "test@email.com");
		Issue issue = Issue.of(repository, "Test Issue", false, "open", LocalDateTime.now().minusDays(1),
			LocalDateTime.now(), null, 100L, new ArrayList<>());

		IssueWithPagedDto issueWithStatus = new IssueWithPagedDto(issue, false);
		Page<IssueWithPagedDto> mockPage = new PageImpl<>(
			List.of(issueWithStatus),
			PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created")),
			1
		);

		when(userService.findGithubUser("testUser")).thenReturn(user);
		when(issueRepository.findIssuesWithPaged(eq(repository.getId()), eq(user.getId()), any(Pageable.class)))
			.thenReturn(mockPage);
		when(labelService.getLabelsByIssueId(issue.getId())).thenReturn(new ArrayList<>());

		// When
		PagedRepositoryIssuesResponse response = issueService.getPagedRepositoryIssuesResponse(
			repository, "created", "desc", "testUser", 0, 10);

		// Then
		assertNotNull(response);
		assertEquals(0, response.getCurrentPage());
		assertEquals(1, response.getTotalElements());
		assertEquals(1, response.getTotalPages());
		assertEquals("testRepo", response.getRepositoryName());
		assertEquals(1, response.getIssues().size());
	}

	@Test
	@DisplayName("사용자가 스타를 준 상위 5개 이슈를 반환한다.")
	void getStaredRepositoryIssuesResponse_shouldReturnTop5StarredIssues() {
		// Given
		String githubId = "testUser";
		User user = new User(githubId, "test@email.com");
		Repository repository = new Repository(null, "testRepo", 123L, LocalDateTime.now());
		Issue issue = Issue.of(repository, "Test Issue", false, "open", LocalDateTime.now().minusDays(1),
			LocalDateTime.now(), null, 100L, new ArrayList<>());

		List<IssueWithStarDto> starredIssues = List.of(
			new IssueWithStarDto(issue, true, "testRepo", "testOrg")
		);

		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(issueRepository.findTop5StarredIssuesForUserWithLabels(user.getId())).thenReturn(starredIssues);
		when(labelService.getLabelsByIssueId(issue.getId())).thenReturn(new ArrayList<>());

		// When
		StarRepositoryIssuesResponse response = issueService.getStarredRepositoryIssuesResponse(githubId);

		// Then
		assertNotNull(response);
		assertEquals(1, response.getIssues().size());
		assertTrue(response.getIssues().get(0).isStarred());
		assertEquals("testOrg", response.getIssues().get(0).getRepositoryName());
	}

	@Test
	@DisplayName("이슈 스타를 토글한다.")
	void toggleIssueStar_shouldToggleIssueStar() {
		// Given
		String githubId = "testUser";
		Long issueId = 100L;
		User user = new User(githubId, "test@email.com");
		Repository repository = new Repository(null, "testRepo", 123L, LocalDateTime.now());
		Issue issue = Issue.of(repository, "Test Issue", false, "open", LocalDateTime.now().minusDays(1),
			LocalDateTime.now(), null, issueId, new ArrayList<>());

		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(issueRepository.findByGhIssueId(issueId)).thenReturn(Optional.of(issue));

		// Case 1: Star does not exist
		when(issueStarRepository.findByUserAndIssue(user, issue)).thenReturn(Optional.empty());

		// When
		issueService.toggleIssueStar(githubId, issueId);

		// Then
		verify(issueStarRepository).save(any());

		// Case 2: Star exists
		IssueStar issueStar = IssueStar.of(user, issue);
		when(issueStarRepository.findByUserAndIssue(user, issue)).thenReturn(Optional.of(issueStar));

		// When
		issueService.toggleIssueStar(githubId, issueId);

		// Then
		verify(issueStarRepository).delete(issueStar);
	}

	@Test
	@DisplayName("이슈 상세 정보와 코멘트를 가져온다.")
	void getIssueDetailAndComments_shouldReturnIssueDetailAndComments() {
		// Given
		String orgName = "testOrg";
		String repoName = "testRepo";
		String issueNumber = "1";
		String githubId = "testUser";
		String accessToken = "testToken";

		IssueDetailDto mockIssueDetail = new IssueDetailDto();
		mockIssueDetail.setTitle("Test Issue");
		mockIssueDetail.setBody("This is a test issue");

		CommentsDto mockComment = new CommentsDto();
		mockComment.setBody("This is a test comment");

		when(githubTokenService.findAccessToken(githubId)).thenReturn(accessToken);
		when(responseSpec.bodyToMono(IssueDetailDto.class)).thenReturn(Mono.just(mockIssueDetail));
		when(responseSpec.bodyToFlux(CommentsDto.class)).thenReturn(Flux.just(mockComment));

		// When
		IssueDetailAndCommentsResponse response = issueService.getIssueDetailAndComments(orgName, repoName, issueNumber,
			githubId);

		// Then
		assertNotNull(response);
		assertEquals("Test Issue", response.getIssueDetailDto().getTitle());
		assertEquals("This is a test issue", response.getIssueDetailDto().getBody());
		assertEquals(1, response.getComments().size());
		assertEquals("This is a test comment", response.getComments().get(0).getBody());

		verify(githubTokenService).findAccessToken(githubId);
		verify(webClient, times(2)).get();
		verify(requestHeadersSpec, times(4)).header(anyString(), anyString());
		verify(requestHeadersSpec, times(2)).retrieve();
		verify(responseSpec).bodyToMono(IssueDetailDto.class);
		verify(responseSpec).bodyToFlux(CommentsDto.class);
	}
}