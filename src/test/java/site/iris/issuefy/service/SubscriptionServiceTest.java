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
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.SubscriptionResponse;

@Slf4j
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private OrgService orgService;

	@Mock
	private RepositoryService repositoryService;

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
		when(subscriptionRepository.findByUserId(user.getId())).thenReturn(List.of(subscription));

		// when
		List<SubscriptionResponse> responses = subscriptionService.getSubscribedRepositories(githubId);

		// then
		assertNotNull(responses);
		assertEquals(1, responses.size());
		assertEquals("testOrg", responses.get(0).org().name());
		assertEquals(1, responses.get(0).org().repositories().size());
		assertEquals("testRepo", responses.get(0).org().repositories().get(0).getName());
	}

	@DisplayName("리포지토리를 구독한다")
	@Test
	void addSubscribeRepository() {
		// given
		RepositoryUrlDto repositoryUrlDto = new RepositoryUrlDto("https://github.com/testOrg/testRepo", "testId",
			"testOrg", "testRepo");
		String githubId = "githubuser1";
		Org org = new Org("organization1", 1L);
		Repository repository = new Repository(org, "repo-a1", 1L);
		User user = new User(githubId, "user1@example.com");

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
		when(orgService.saveOrg(any(ResponseEntity.class))).thenReturn(org);

		when(repositoryService.saveRepository(any(ResponseEntity.class), eq(org)))
			.thenReturn(repository);

		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		when(subscriptionRepository.findByUserIdAndRepository_GhRepoId(user.getId(), repository.getGhRepoId()))
			.thenReturn(Optional.empty());
		when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ReflectionTestUtils.setField(subscriptionService, "ORG_REQUEST_URL", orgRequestUrl);
		ReflectionTestUtils.setField(subscriptionService, "REPOSITORY_REQUEST_URL", repoRequestUrl);

		// when
		subscriptionService.addSubscribeRepository(repositoryUrlDto, githubId);

		// then
		verify(subscriptionRepository, times(1)).save(any(Subscription.class));
	}

	@DisplayName("리포지토리 구독을 삭제한다")
	@Test
	void unsubscribeRepository() {
		// given
		Long ghRepoId = 123L;

		// when
		subscriptionRepository.deleteByRepository_GhRepoId(ghRepoId);

		// then
		verify(subscriptionRepository, times(1)).deleteByRepository_GhRepoId(ghRepoId);
	}

	@Test
	@DisplayName("리포지토리의 즐겨찾기 상태를 토글한다.")
	void testStarRepository() {
		// given
		Long ghRepoId = 1L;

		// when
		subscriptionService.starRepository(ghRepoId);

		// then
		verify(repositoryService, times(1)).updateRepositoryStar(ghRepoId);
	}
}