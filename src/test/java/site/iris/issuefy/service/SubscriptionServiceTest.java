package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.eums.GithubUrls;
import site.iris.issuefy.exception.github.GithubApiException;
import site.iris.issuefy.exception.resource.SubscriptionNotFoundException;
import site.iris.issuefy.exception.resource.SubscriptionPageNotFoundException;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.response.PagedSubscriptionResponse;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Mock
	private UserService userService;

	@Mock
	private OrgService orgService;

	@Mock
	private RepositoryService repositoryService;

	@Mock
	private GithubTokenService githubTokenService;

	@InjectMocks
	private SubscriptionService subscriptionService;

	private MockWebServer mockWebServer;

	@BeforeEach
	void setup() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
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
		User user = new User(1L, githubId, "testuser@example.com", false);
		Org org = new Org("testOrg", 123L);
		Repository repository = new Repository(org, "testRepo", 123L, LocalDateTime.now());
		Subscription subscription = new Subscription(user, repository);
		Pageable pageable = PageRequest.of(1, 15, Sort.by(Sort.Direction.ASC, "repository.latestUpdateAt"));

		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(subscriptionRepository.findPageByUserId(user.getId(), pageable)).thenReturn(
			Optional.of(new PageImpl<>(List.of(subscription))));

		// when
		PagedSubscriptionResponse responses = subscriptionService.getSubscribedRepositories(githubId, 1, 15,
			"latestUpdateAt", "asc", false);

		// then
		assertNotNull(responses);
		assertEquals(1, responses.getSubscriptionListDtos().size());
		assertEquals("testOrg", responses.getSubscriptionListDtos().get(0).getOrgName());
		assertEquals("testRepo", responses.getSubscriptionListDtos().get(0).getRepositoryName());
	}

	@DisplayName("즐겨찾기한 리포지토리 목록을 가져온다")
	@Test
	void getStarredRepositories() {
		// given
		String githubId = "testUser";
		User user = new User(1L, githubId, "testuser@example.com", false);
		Org org = new Org("testOrg", 123L);
		Repository repository = new Repository(org, "testRepo", 123L, LocalDateTime.now());
		Subscription subscription = new Subscription(1L, user, repository, true);
		Pageable pageable = PageRequest.of(1, 15, Sort.by(Sort.Direction.ASC, "repository.latestUpdateAt"));

		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(subscriptionRepository.findPageByUserIdAndRepoStarredTrue(user.getId(), pageable)).thenReturn(
			Optional.of(new PageImpl<>(List.of(subscription))));

		// when
		PagedSubscriptionResponse responses = subscriptionService.getSubscribedRepositories(githubId, 1, 15,
			"latestUpdateAt", "asc", true);

		// then
		assertNotNull(responses);
		assertEquals(1, responses.getSubscriptionListDtos().size());
		assertEquals("testOrg", responses.getSubscriptionListDtos().get(0).getOrgName());
		assertEquals("testRepo", responses.getSubscriptionListDtos().get(0).getRepositoryName());
		assertTrue(responses.getSubscriptionListDtos().get(0).isRepositoryStarred());
	}

	@DisplayName("리포지토리를 구독한다")
	@Test
	void addSubscribeRepository() {
		// given
		RepositoryUrlDto repositoryUrlDto = new RepositoryUrlDto("https://github.com/testOrg/testRepo", "testId",
			"testOrg", "testRepo");
		String githubId = "githubuser1";
		Org org = new Org("organization1", 1L);
		Repository repository = new Repository(org, "repo-a1", 1L, LocalDateTime.now());
		User user = new User(githubId, "user1@example.com");

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"id\":123,\"login\":\"testOrg\"}"));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody("{\"id\":123,\"name\":\"testRepo\"}"));

		String baseUrl = mockWebServer.url("/").toString();

		ReflectionTestUtils.setField(GithubUrls.ORG_REQUEST_URL, "url", baseUrl + "orgs/");
		ReflectionTestUtils.setField(GithubUrls.REPOSITORY_REQUEST_URL, "url", baseUrl + "repos/");

		when(githubTokenService.findAccessToken(githubId)).thenReturn("testAccessToken");
		when(orgService.saveOrg(any(ResponseEntity.class))).thenReturn(org);
		when(repositoryService.saveRepository(any(ResponseEntity.class), eq(org))).thenReturn(repository);
		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(subscriptionRepository.findByUserIdAndRepository_GhRepoId(user.getId(),
			repository.getGhRepoId())).thenReturn(Optional.empty());
		when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
		subscriptionService.unsubscribeRepository(ghRepoId);

		// then
		verify(subscriptionRepository, times(1)).deleteByRepository_GhRepoId(ghRepoId);
	}

	@Test
	@DisplayName("리포지토리의 즐겨찾기 상태를 토글한다")
	void toggleRepositoryStar() {
		// given
		String githubId = "testUser";
		Long ghRepoId = 1L;
		User user = new User(1L, githubId, "testuser@example.com", false);
		Repository repository = new Repository(1L, new Org(), "testRepo", ghRepoId, LocalDateTime.now());
		Subscription subscription = new Subscription(user, repository);

		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(subscriptionRepository.findByUserIdAndRepository_GhRepoId(user.getId(), ghRepoId)).thenReturn(
			Optional.of(subscription));

		// when
		subscriptionService.toggleRepositoryStar(githubId, ghRepoId);

		// then
		assertTrue(subscription.isRepoStarred());
		verify(subscriptionRepository, times(1)).save(subscription);
	}

	@Test
	@DisplayName("존재하지 않는 구독에 대해 즐겨찾기 상태를 토글하려고 하면 예외가 발생한다")
	void toggleRepositoryStar_WithNonExistentSubscription() {
		// given
		String githubId = "testUser";
		Long ghRepoId = 1L;
		User user = new User(1L, githubId, "testuser@example.com", false);

		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(subscriptionRepository.findByUserIdAndRepository_GhRepoId(user.getId(), ghRepoId)).thenReturn(
			Optional.empty());

		// when & then
		assertThrows(SubscriptionNotFoundException.class,
			() -> subscriptionService.toggleRepositoryStar(githubId, ghRepoId));
		verify(subscriptionRepository, never()).save(any(Subscription.class));
	}

	@Test
	@DisplayName("GitHub API 호출 중 예외가 발생하면 GithubApiException을 던진다")
	void addSubscribeRepository_WithGithubApiException() {
		// given
		RepositoryUrlDto repositoryUrlDto = new RepositoryUrlDto("https://github.com/testOrg/testRepo", "testId",
			"testOrg", "testRepo");
		String githubId = "githubuser1";

		mockWebServer.enqueue(new MockResponse().setResponseCode(404));
		when(githubTokenService.findAccessToken(githubId)).thenReturn("testAccessToken");

		// when & then
		assertThrows(GithubApiException.class,
			() -> subscriptionService.addSubscribeRepository(repositoryUrlDto, githubId));
	}

	@Test
	@DisplayName("구독 페이지를 찾을 수 없을 때 SubscriptionPageNotFoundException을 던진다")
	void getSubscribedRepositories_WithNonExistentPage() {
		// given
		String githubId = "testUser";
		User user = new User(1L, githubId, "testuser@example.com", false);
		Pageable pageable = PageRequest.of(1, 15, Sort.by(Sort.Direction.ASC, "repository.latestUpdateAt"));

		when(userService.findGithubUser(githubId)).thenReturn(user);
		when(subscriptionRepository.findPageByUserId(user.getId(), pageable)).thenReturn(Optional.empty());

		// when & then
		assertThrows(SubscriptionPageNotFoundException.class,
			() -> subscriptionService.getSubscribedRepositories(githubId, 1, 15,
				"latestUpdateAt", "asc", false));
	}
}