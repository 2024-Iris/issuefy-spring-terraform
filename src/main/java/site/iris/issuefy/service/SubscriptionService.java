package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.exception.UserNotFoundException;
import site.iris.issuefy.model.dto.GithubOrgDto;
import site.iris.issuefy.model.dto.GithubRepositoryDto;
import site.iris.issuefy.model.dto.RepositoryDto;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.model.vo.OrgRecord;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.SubscrptionResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
	// TODO Enum으로 변경
	private static String ORG_REQUEST_URL = "https://api.github.com/orgs/";
	private static String REPOSITORY_REQUEST_URL = "https://api.github.com/repos/";
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;
	private final OrgService orgService;
	private final RepositoryService repositoryService;
	private final GithubTokenService githubTokenService;

	public List<SubscrptionResponse> getSubscribedRepositories(String githubId) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserNotFoundException(githubId));
		List<Subscription> subscriptions = subscriptionRepository.findByUserId(user.getId());

		Map<OrgRecord, List<RepositoryDto>> orgResponseMap = new HashMap<>();
		for (Subscription subscription : subscriptions) {
			Long orgId = subscription.getRepository().getOrg().getGhOrgId();
			String orgName = subscription.getRepository().getOrg().getName();
			RepositoryDto repositoryDto = RepositoryDto.of(subscription.getRepository().getGhRepoId(),
				subscription.getRepository().getName(), subscription.getRepository().isStarred());

			OrgRecord orgRecord = OrgRecord.from(orgId, orgName, new ArrayList<>());

			if (!orgResponseMap.containsKey(orgRecord)) {
				orgResponseMap.put(orgRecord, new ArrayList<>());
			}
			orgResponseMap.get(orgRecord).add(repositoryDto);
		}

		List<SubscrptionResponse> responses = new ArrayList<>();
		for (Map.Entry<OrgRecord, List<RepositoryDto>> entry : orgResponseMap.entrySet()) {
			OrgRecord orgRecord = OrgRecord.from(entry.getKey().id(), entry.getKey().name(), entry.getValue());
			responses.add(SubscrptionResponse.from(orgRecord));
		}

		return responses;
	}

	@Transactional
	public void addSubscribeRepository(RepositoryUrlDto repositoryUrlDto, String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);

		ResponseEntity<GithubOrgDto> orgInfo = getOrgInfo(repositoryUrlDto, accessToken);
		ResponseEntity<GithubRepositoryDto> repositoryInfo = getRepositoryInfo(repositoryUrlDto, accessToken);

		Org org = orgService.saveOrg(orgInfo);
		Repository repository = repositoryService.saveRepository(repositoryInfo, org);

		User user = userRepository.findByGithubId(repositoryUrlDto.getGithubId())
			.orElseThrow();
		saveSubscription(user, repository);
	}

	@Transactional
	public void unsubscribeRepository(Long ghRepoId) {
		subscriptionRepository.deleteByRepository_GhRepoId(ghRepoId);
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

	private void saveSubscription(User user, Repository repository) {
		subscriptionRepository.findByUserIdAndRepositoryId(user.getId(), repository.getId())
			.orElseGet(() -> {
				Subscription newSubscription = new Subscription(user, repository);
				return subscriptionRepository.save(newSubscription);
			});
	}
}
