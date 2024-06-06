package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscribe;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.repository.OrgRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.repository.SubscribeRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.SubscribeResponse;

@ExtendWith(MockitoExtension.class)
class SubscribeServiceTest {

	@Mock
	private SubscribeRepository subscribeRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private OrgRepository orgRepository;

	@Mock
	private RepositoryRepository repositoryRepository;

	@InjectMocks
	private SubscribeService subscribeService;

	@DisplayName("구독한 레포지토리 목록을 가져온다")
	@Test
	void getSubscribedRepositories() {
		// given
		String githubId = "testUser";
		User user = new User(githubId, "testuser@example.com");
		Org org = new Org("testOrg", 123L);
		Repository repository = new Repository(org, "testRepo", 123L);
		Subscribe subscribe = new Subscribe(user, repository);

		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		when(subscribeRepository.findByUserId(user.getId())).thenReturn(List.of(subscribe));

		// when
		List<SubscribeResponse> responses = subscribeService.getSubscribedRepositories(githubId);

		// then
		assertNotNull(responses);
		assertEquals(1, responses.size());
		assertEquals("testOrg", responses.get(0).org().name());
		assertEquals(1, responses.get(0).org().repositories().size());
		assertEquals("testRepo", responses.get(0).org().repositories().get(0).getName());
	}

	@DisplayName("레포지토리를 구독한다")
	@Test
	void addSubscribeRepository() {
		// given
		RepositoryUrlDto repositoryUrlDto = new RepositoryUrlDto("https://github.com/testOrg/testRepo", "testId",
			"testOrg", "testRepo");
		String githubId = "testUser";
		Org org = new Org("testOrg", 123L);
		Repository repository = new Repository(org, "testRepo", 123L);
		User user = new User(githubId, "testuser@example.com");
		Subscribe subscribe = new Subscribe(user, repository);

		when(orgRepository.findByName(repositoryUrlDto.getOrgName())).thenReturn(Optional.of(org));
		when(repositoryRepository.findByNameAndOrgId(repositoryUrlDto.getRepositoryName(), org.getId())).thenReturn(
			Optional.of(repository));
		when(userRepository.findByGithubId(repositoryUrlDto.getGithubId())).thenReturn(Optional.of(user));
		when(subscribeRepository.findByUserIdAndRepositoryId(user.getId(), repository.getId())).thenReturn(
			Optional.of(subscribe));

		// when
		subscribeService.addSubscribeRepository(repositoryUrlDto, githubId);

		// then
		verify(orgRepository, times(1)).findByName(repositoryUrlDto.getOrgName());
		verify(repositoryRepository, times(1)).findByNameAndOrgId(repositoryUrlDto.getRepositoryName(), org.getId());
		verify(userRepository, times(1)).findByGithubId(repositoryUrlDto.getGithubId());
		verify(subscribeRepository, times(1)).findByUserIdAndRepositoryId(user.getId(), repository.getId());
		verify(subscribeRepository, never()).save(any(Subscribe.class));
	}
}