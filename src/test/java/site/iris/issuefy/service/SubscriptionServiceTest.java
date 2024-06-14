package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.repository.OrgRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.repository.SubscribeRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.SubscribeResponse;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

	@Mock
	private SubscribeRepository subscribeRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private OrgRepository orgRepository;

	@Mock
	private RepositoryRepository repositoryRepository;

	@InjectMocks
	private SubscriptionService subscriptionService;

	@Mock
	private GithubTokenService githubTokenService;

	private MockWebServer mockWebServer;

	@BeforeEach
	void setup() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		mockWebServer.enqueue(new MockResponse()
			.setBody("{\"login\": \"testUser\", \"avatar_url\": \"testUserUrl\"}")
			.addHeader("Content-Type", "application/json")
			.setResponseCode(200));
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@DisplayName("구독한 리포지토리 목록을 가져온다")
	@Test
	void getSubscribedRepositories() {
		// given
		String githubId = "testUser";
		User user = new User(githubId, "testuser@example.com");
		Org org = new Org("testOrg", 123L);
		Repository repository = new Repository(org, "testRepo", 123L);
		Subscription subscription = new Subscription(user, repository);

		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		when(subscribeRepository.findByUserId(user.getId())).thenReturn(List.of(subscription));

		// when
		List<SubscribeResponse> responses = subscriptionService.getSubscribedRepositories(githubId);

		// then
		assertNotNull(responses);
		assertEquals(1, responses.size());
		assertEquals("testOrg", responses.get(0).org().name());
		assertEquals(1, responses.get(0).org().repositories().size());
		assertEquals("testRepo", responses.get(0).org().repositories().get(0).getName());
	}

	@DisplayName("리포지토리를 구독한다")
	@Test
	void addSubscribeRepository() throws Exception {
		// given
		RepositoryUrlDto repositoryUrlDto = new RepositoryUrlDto("https://github.com/testOrg/testRepo", "testId",
			"testOrg", "testRepo");
		String githubId = "testUser";
		Org org = new Org("testOrg", 123L);
		Repository repository = new Repository(org, "testRepo", 123L);
		User user = new User(githubId, "testuser@example.com");
		Subscription subscription = new Subscription(user, repository);

		// Mock 서버 설정
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"id\":123,\"login\":\"testOrg\"}"));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"id\":123,\"name\":\"testRepo\"}"));

		String baseUrl = mockWebServer.url("/").toString();
		String orgRequestUrl = baseUrl + "orgs/";
		String repoRequestUrl = baseUrl + "repos/";

		when(githubTokenService.findAccessToken(githubId)).thenReturn("testAccessToken");
		when(orgRepository.findByName(repositoryUrlDto.getOrgName())).thenReturn(Optional.of(org));
		when(repositoryRepository.findByGhRepoId(123L)).thenReturn(Optional.of(repository));
		when(userRepository.findByGithubId(repositoryUrlDto.getGithubId())).thenReturn(Optional.of(user));
		when(subscribeRepository.findByUserIdAndRepositoryId(user.getId(), repository.getId())).thenReturn(
			Optional.of(subscription));

		ReflectionTestUtils.setField(subscriptionService, "ORG_REQUEST_URL", orgRequestUrl);
		ReflectionTestUtils.setField(subscriptionService, "REPOSITORY_REQUEST_URL", repoRequestUrl);

		// when
		subscriptionService.addSubscribeRepository(repositoryUrlDto, githubId);

		// then
		verify(orgRepository, times(1)).findByName(repositoryUrlDto.getOrgName());
		verify(repositoryRepository, times(1)).findByGhRepoId(123L);
		verify(userRepository, times(1)).findByGithubId(repositoryUrlDto.getGithubId());
		verify(subscribeRepository, times(1)).findByUserIdAndRepositoryId(user.getId(), repository.getId());
		verify(subscribeRepository, never()).save(any(Subscription.class));
	}

	@DisplayName("리포지토리 구독을 삭제한다")
	@Test
	void unsubscribeRepository() {
		// given
		Long ghRepoId = 123L;

		// when
		subscribeRepository.deleteByRepository_GhRepoId(ghRepoId);

		// then
		verify(subscribeRepository, times(1)).deleteByRepository_GhRepoId(ghRepoId);
	}
}