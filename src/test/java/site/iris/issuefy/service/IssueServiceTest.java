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
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.model.dto.IssueWithStarStatusDto;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;

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
	@DisplayName("getRepositoryIssues: 리포지토리 이슈를 가져오고 동기화한다")
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
		IssueWithStarStatusDto mockIssueWithStatus = new IssueWithStarStatusDto(mockIssue, false);
		Page<IssueWithStarStatusDto> mockPage = new PageImpl<>(
			List.of(mockIssueWithStatus),
			PageRequest.of(0, 10),
			1
		);
		when(issueRepository.findIssuesWithStarStatus(eq(repository.getId()), eq(user.getId()), any(Pageable.class)))
			.thenReturn(mockPage);

		// When
		PagedRepositoryIssuesResponse response = issueService.getRepositoryIssues(orgName, repoName, githubId, 0, 10,
			"created", "desc");

		// Then
		assertNotNull(response);
		verify(issueRepository, times(1)).saveAll(anyList());
		verify(labelService, times(1)).saveAllLabels(anyList());
		verify(issueLabelRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("synchronizeRepositoryIssues: 새 이슈를 추가한다")
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
		verify(issueLabelRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("updateExistingIssues: GitHub에서 가져온 이슈가 로컬 이슈보다 최신이고 다른 이슈일 때 업데이트한다")
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
		verify(issueLabelRepository, times(1)).saveAll(anySet());
	}

	@Test
	@DisplayName("updateExistingIssues: GitHub에서 가져온 이슈가 로컬 이슈보다 최신이지만 같은 이슈일 때 업데이트하지 않는다")
	void updateExistingIssues_whenGithubIssueIsNewerButSame_shouldNotUpdate() {
		// Given
		Org org = new Org("testOrg", 1L);

		// Repository mock 설정
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

		IssueDto newerGithubIssue = IssueDto.of(1L, "Updated Issue", false, "open", newDate,
			newDate, null, new ArrayList<>());

		when(githubTokenService.findAccessToken(anyString())).thenReturn("testToken");
		when(responseSpec.bodyToFlux(IssueDto.class)).thenReturn(Flux.just(newerGithubIssue));

		// When
		issueService.updateExistingIssues("testOrg", "testRepo", "testUser", mockedRepository);

		// Then
		verify(issueRepository, never()).saveAll(anyList());
		verify(issueLabelRepository, never()).saveAll(anyList());
	}

	@Test
	@DisplayName("createPagedRepositoryIssuesResponse: 페이지네이션된 이슈 응답을 생성한다")
	void getPagedRepositoryIssuesResponse_shouldReturnPagedResponse() {
		// Given
		Repository repository = new Repository(null, "testRepo", 123L, LocalDateTime.now());
		User user = new User("testUser", "test@email.com");
		Issue issue = Issue.of(repository, "Test Issue", false, "open", LocalDateTime.now().minusDays(1),
			LocalDateTime.now(), null, 100L, new ArrayList<>());

		IssueWithStarStatusDto issueWithStatus = new IssueWithStarStatusDto(issue, false);
		Page<IssueWithStarStatusDto> mockPage = new PageImpl<>(
			List.of(issueWithStatus),
			PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created")),
			1
		);

		when(userService.findGithubUser("testUser")).thenReturn(user);
		when(issueRepository.findIssuesWithStarStatus(eq(repository.getId()), eq(user.getId()), any(Pageable.class)))
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
}