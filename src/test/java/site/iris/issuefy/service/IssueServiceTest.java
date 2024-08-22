package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Flux;
import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.IssueDto;
import site.iris.issuefy.model.dto.IssueWithStarStatusDto;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;

@Slf4j
class IssueServiceTest {
	MockWebServer mockWebServer;

	@Mock
	private IssueRepository issueRepository;

	@Mock
	private RepositoryRepository repositoryRepository;

	@Mock
	private GithubTokenService githubTokenService;

	@Mock
	private LabelService labelService;

	@Mock
	private IssueLabelRepository issueLabelRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private WebClient webClient;

	@InjectMocks
	private IssueService issueService;

	@BeforeEach
	@DisplayName("MockWebServer로 GitHub API의 응답을 모킹합니다.")
	void setUp() throws IOException {
		MockitoAnnotations.openMocks(this);

		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockWebServer.enqueue(new MockResponse()
			.setBody(
				"[{\"title\": \"testIssue\", \"state\": \"open\", \"ghIssueId\": 12345, \"labels\": [{\"name\": \"bug\", \"color\": \"f29513\"}]}]")
			.addHeader("Content-Type", "application/json")
			.setResponseCode(200));

		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();

		issueService = new IssueService(webClient, githubTokenService, issueRepository, repositoryRepository,
			labelService, issueLabelRepository, userRepository);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@DisplayName("구독한 리포지토리의 오픈되어 있는 good first issue를 저장한다")
	@Test
	void getRepositoryIssues_issuesSaved() {
		// given
		Org org = new Org("testOrg", 2);
		Repository repository = new Repository(org, "testRepo", 123, LocalDateTime.now());
		User user = new User("test", "test@email.test");

		when(repositoryRepository.findByName(anyString())).thenReturn(Optional.of(repository));
		when(userRepository.findByGithubId(anyString())).thenReturn(Optional.of(user));
		when(issueRepository.existsById(anyLong())).thenReturn(false);

		// GitHub API 모킹
		IssueDto mockIssueDto = IssueDto.of(1L, "testTitle", false, "open", LocalDateTime.now().minusDays(2),
			LocalDateTime.now().minusDays(1), null, new ArrayList<>());

		WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
		WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
		WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

		when(webClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToFlux(IssueDto.class)).thenReturn(Flux.just(mockIssueDto));

		// findIssuesWithStarStatus 모킹
		Issue mockIssue = Issue.of(repository, "Test Issue", false, "open", LocalDateTime.now(), LocalDateTime.now(),
			null, 1L, new ArrayList<>());

		IssueWithStarStatusDto mockIssueWithStarStatusDto = new IssueWithStarStatusDto(mockIssue, false);
		Page<IssueWithStarStatusDto> mockPage = new PageImpl<>(
			List.of(mockIssueWithStarStatusDto),
			PageRequest.of(0, 10),
			1
		);
		when(issueRepository.findIssuesWithStarStatus(eq(repository.getId()), eq(user.getId()), any(Pageable.class)))
			.thenReturn(mockPage);

		// when
		PagedRepositoryIssuesResponse response = issueService.getRepositoryIssues("testOrg", repository.getName(),
			user.getGithubId(), 0, 10, "created", "desc");

		// then
		assertNotNull(response);
		verify(issueRepository, times(1)).saveAll(anyList());
		verify(labelService, times(1)).saveAllLabels(anyList());
		verify(issueLabelRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("getRepositoryIssues: 새 이슈 추가 테스트")
	void getRepositoryIssues_whenNoExistingIssues_shouldAddNewIssues() {
		// Given
		String orgName = "testOrg";
		String repoName = "testRepo";
		String githubId = "testUser";

		Org org = new Org("testOrg", 2);
		Repository repository = new Repository(org, "testRepo", 123, LocalDateTime.now());
		User user = new User("test", "test@email.test");

		when(repositoryRepository.findByName(repoName)).thenReturn(Optional.of(repository));
		when(issueRepository.existsById(repository.getId())).thenReturn(false);
		when(githubTokenService.findAccessToken(githubId)).thenReturn("testToken");

		// GitHub API 응답 모킹
		IssueDto mockIssueDto = IssueDto.of(1L, "testTitle", false, "open", LocalDateTime.now().minusDays(2),
			LocalDateTime.now().minusDays(1), null, new ArrayList<>());

		WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
		WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
		WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

		when(webClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToFlux(IssueDto.class)).thenReturn(Flux.just(mockIssueDto));

		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));

		Page<IssueWithStarStatusDto> mockPage = new PageImpl<>(
			List.of(new IssueWithStarStatusDto(new Issue(), false)),
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
}