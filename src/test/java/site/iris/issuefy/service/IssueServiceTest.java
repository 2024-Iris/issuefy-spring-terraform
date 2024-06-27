package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.exception.RepositoryNotFoundException;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.IssueRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.response.RepositoryIssuesResponse;

public class IssueServiceTest {
	MockWebServer mockWebServer;

	@Mock
	private IssueRepository issueRepository;

	@Mock
	private RepositoryRepository repositoryRepository;

	@Mock
	GithubTokenService githubTokenService;

	@Mock
	private LabelService labelService;

	@Mock
	IssueLabelRepository issueLabelRepository;

	@InjectMocks
	private IssueService issueService;

	@BeforeEach
	@DisplayName("MockWebServer로 GitHub API의 응답을 모킹합니다.")
	void setUp() throws IOException {
		MockitoAnnotations.openMocks(this);

		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockWebServer.enqueue(new MockResponse()
			.setBody("[{\"title\": \"testIssue\", \"state\": \"open\", \"ghIssueId\": 12345, \"labels\": [{\"name\": \"bug\", \"color\": \"f29513\"}]}]")
			.addHeader("Content-Type", "application/json")
			.setResponseCode(200));

		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();

		issueService = new IssueService(webClient, githubTokenService, issueRepository, repositoryRepository,
			labelService, issueLabelRepository);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@DisplayName("구독한 리포지토리의 오픈되어 있는 good first issue를 저장한다")
	@Test
	void initializeIssueSubscription_issueSaved() {
		// given
		Org org = new Org("testOrg", 123456L);
		Repository repository = new Repository(org, "testRepo", 1L);
		when(repositoryRepository.findByName(anyString())).thenReturn(Optional.of(repository));

		// when
		RepositoryIssuesResponse response = issueService.initializeIssueSubscription(repository.getOrg().getName(), repository.getName(), "dokkisan");

		// then
		assertNotNull(response);
		verify(issueRepository, times(1)).saveAll(anyList());
		verify(labelService, times(1)).saveAllLabels(anyList());
		verify(issueLabelRepository, times(1)).saveAll(anyList());
	}
}
