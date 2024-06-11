package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscribe;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.exception.UserNotFoundException;
import site.iris.issuefy.model.dto.GithubOrgDto;
import site.iris.issuefy.model.dto.GithubRepositoryDto;
import site.iris.issuefy.model.dto.RepositoryDto;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.model.vo.OrgRecord;
import site.iris.issuefy.repository.OrgRepository;
import site.iris.issuefy.repository.RepositoryRepository;
import site.iris.issuefy.repository.SubscribeRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.SubscribeResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscribeService {
	// TODO Enum으로 변경
	private static String ORG_REQUEST_URL = "https://api.github.com/orgs/";
	private static String REPOSITORY_REQUEST_URL = "https://api.github.com/repos/";
	private final SubscribeRepository subscribeRepository;
	private final UserRepository userRepository;
	private final OrgRepository orgRepository;
	private final RepositoryRepository repositoryRepository;
	private final GithubTokenService githubTokenService;

	public List<SubscribeResponse> getSubscribedRepositories(String githubId) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserNotFoundException(githubId));
		List<Subscribe> subscribes = subscribeRepository.findByUserId(user.getId());

		Map<OrgRecord, List<RepositoryDto>> orgResponseMap = new HashMap<>();
		for (Subscribe subscribe : subscribes) {
			Long orgId = subscribe.getRepository().getOrg().getGhOrgId();
			String orgName = subscribe.getRepository().getOrg().getName();
			RepositoryDto repositoryDto = RepositoryDto.of(subscribe.getRepository().getGhRepoId(),
				subscribe.getRepository().getName(), subscribe.getRepository().isStarred());

			OrgRecord orgRecord = OrgRecord.from(orgId, orgName, new ArrayList<>());

			if (!orgResponseMap.containsKey(orgRecord)) {
				orgResponseMap.put(orgRecord, new ArrayList<>());
			}
			orgResponseMap.get(orgRecord).add(repositoryDto);
		}

		List<SubscribeResponse> responses = new ArrayList<>();
		for (Map.Entry<OrgRecord, List<RepositoryDto>> entry : orgResponseMap.entrySet()) {
			OrgRecord orgRecord = OrgRecord.from(entry.getKey().id(), entry.getKey().name(), entry.getValue());
			responses.add(SubscribeResponse.from(orgRecord));
		}

		return responses;
	}

	//TODO 리펙터링 필요
	@Transactional
	public void addSubscribeRepository(RepositoryUrlDto repositoryUrlDto, String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);
		ResponseEntity<GithubOrgDto> orgInfo = getOrgInfo(repositoryUrlDto, accessToken);
		ResponseEntity<GithubRepositoryDto> repositoryInfo = getRepositoryInfo(repositoryUrlDto, accessToken);

		Org org = orgRepository.findByName(repositoryUrlDto.getOrgName())
			.orElseGet(() -> {
				Org newOrg = new Org(orgInfo.getBody().getLogin(), orgInfo.getBody().getId());
				return orgRepository.save(newOrg);
			});
		Repository repository = repositoryRepository.findByGhRepoId(
				repositoryInfo.getBody().getId())
			.orElseGet(() -> {
				Repository newRepository = new Repository(org, repositoryInfo.getBody().getName(),
					repositoryInfo.getBody().getId());
				return repositoryRepository.save(newRepository);
			});
		User user = userRepository.findByGithubId(repositoryUrlDto.getGithubId())
			.orElseGet(() -> {
				User newUser = new User(repositoryUrlDto.getGithubId(), githubId);
				return userRepository.save(newUser);
			});
		Subscribe subscribe = subscribeRepository.findByUserIdAndRepositoryId(user.getId(), repository.getId())
			.orElseGet(() -> {
				Subscribe newSubscribe = new Subscribe(user, repository);
				return subscribeRepository.save(newSubscribe);
			});
	}

	public void unsubscribeRepository(Long ghRepoId) {
		subscribeRepository.deleteByRepository_GhRepoId(ghRepoId);
	}

	public ResponseEntity<GithubOrgDto> getOrgInfo(RepositoryUrlDto repositoryUrlDto, String accessToken) {
		return WebClient.create()
			.get()
			.uri(ORG_REQUEST_URL + repositoryUrlDto.getOrgName())
			.headers(headers -> {
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setBearerAuth(accessToken);
			})
			.retrieve()
			.toEntity(GithubOrgDto.class)
			.block();
	}

	public ResponseEntity<GithubRepositoryDto> getRepositoryInfo(RepositoryUrlDto repositoryUrlDto,
		String accessToken) {
		return WebClient.create()
			.get()
			.uri(REPOSITORY_REQUEST_URL + repositoryUrlDto.getOrgName() + "/" + repositoryUrlDto.getRepositoryName())
			.headers(headers -> {
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setBearerAuth(accessToken);
			})
			.retrieve()
			.toEntity(GithubRepositoryDto.class)
			.block();
	}
}
