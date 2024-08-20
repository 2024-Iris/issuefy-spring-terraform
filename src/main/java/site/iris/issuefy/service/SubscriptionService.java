package site.iris.issuefy.service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.exception.github.GithubApiException;
import site.iris.issuefy.exception.resource.SubscriptionNotFoundException;
import site.iris.issuefy.exception.resource.SubscriptionPageNotFoundException;
import site.iris.issuefy.exception.resource.UserNotFoundException;
import site.iris.issuefy.model.dto.GithubOrgDto;
import site.iris.issuefy.model.dto.GithubRepositoryDto;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.model.dto.SubscriptionListDto;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.response.PagedSubscriptionResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
	private static String ORG_REQUEST_URL = "https://api.github.com/orgs/";
	private static String REPOSITORY_REQUEST_URL = "https://api.github.com/repos/";
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;
	private final OrgService orgService;
	private final RepositoryService repositoryService;
	private final GithubTokenService githubTokenService;

	public PagedSubscriptionResponse getSubscribedRepositories(String githubId, int page, int pageSize, String sort,
		String order, boolean starred) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserNotFoundException(ErrorCode.NOT_EXIST_USER.getMessage(),
				ErrorCode.NOT_EXIST_USER.getStatus(), githubId));

		Sort.Direction direction = Sort.Direction.fromString(order);
		Sort sorting = Sort.by(direction, getActualSortProperty(sort));
		Pageable pageable = PageRequest.of(page, pageSize, sorting);

		Page<Subscription> subscriptions = getSubscriptions(starred, user, pageable);

		List<SubscriptionListDto> subscriptionListDtos = subscriptions.getContent().stream()
			.map(subscription -> SubscriptionListDto.of(
				subscription.getRepository().getOrg().getGhOrgId(),
				subscription.getRepository().getOrg().getName(),
				subscription.getRepository().getGhRepoId(),
				subscription.getRepository().getName(),
				subscription.getRepository().getLatestUpdateAt(),
				subscription.isRepoStarred()
			))
			.toList();

		return PagedSubscriptionResponse.of(
			subscriptions.getNumber(),
			subscriptions.getSize(),
			subscriptions.getTotalElements(),
			subscriptions.getTotalPages(),
			subscriptionListDtos
		);
	}

	private Page<Subscription> getSubscriptions(boolean starred, User user, Pageable pageable) {
		Page<Subscription> subscriptions;
		if (starred) {
			subscriptions = subscriptionRepository.findPageByUserIdAndRepoStarredTrue(user.getId(), pageable)
				.orElseThrow(
					() -> new SubscriptionPageNotFoundException(
						ErrorCode.USER_SUBSCRIPTIONS_PAGE_NOT_FOUND.getMessage(),
						ErrorCode.USER_SUBSCRIPTIONS_PAGE_NOT_FOUND.getStatus(), user.getGithubId()));
		} else {
			subscriptions = subscriptionRepository.findPageByUserId(user.getId(), pageable)
				.orElseThrow(() -> new SubscriptionPageNotFoundException(
					ErrorCode.USER_STARRED_SUBSCRIPTIONS_PAGE_NOT_FOUND.getMessage(),
					ErrorCode.USER_STARRED_SUBSCRIPTIONS_PAGE_NOT_FOUND.getStatus(), user.getGithubId()));
		}
		return subscriptions;
	}

	private String getActualSortProperty(String sort) {
		return switch (sort) {
			case "repositoryName" -> "repository.name";
			case "orgName" -> "repository.org.name";
			default -> "repository.latestUpdateAt";
		};
	}

	@Transactional
	public void addSubscribeRepository(RepositoryUrlDto repositoryUrlDto, String githubId) {
		String accessToken = githubTokenService.findAccessToken(githubId);

		ResponseEntity<GithubOrgDto> orgInfo = githubGetOrgInfo(repositoryUrlDto, accessToken);
		ResponseEntity<GithubRepositoryDto> repositoryInfo = githubGetRepositoryInfo(repositoryUrlDto, accessToken);

		Org org = orgService.saveOrg(orgInfo);
		Repository repository = repositoryService.saveRepository(repositoryInfo, org);
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserNotFoundException(ErrorCode.NOT_EXIST_USER.getMessage(),
				ErrorCode.NOT_EXIST_USER.getStatus(), githubId));
		saveSubscription(user, repository);
	}

	// TODO: 연관된 리포지토리를 아무도 구독하고 있지 않다면 리포지토리 삭제 로직 추가
	@Transactional
	public void unsubscribeRepository(Long ghRepoId) {
		subscriptionRepository.deleteByRepository_GhRepoId(ghRepoId);
	}

	@Transactional
	public void toggleRepositoryStar(String githubId, Long ghRepoId) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserNotFoundException(ErrorCode.NOT_EXIST_USER.getMessage(),
				ErrorCode.NOT_EXIST_USER.getStatus(), githubId));

		Subscription subscription = subscriptionRepository.findByUserIdAndRepository_GhRepoId(user.getId(),
				ghRepoId)
			.orElseThrow(() -> new SubscriptionNotFoundException(ErrorCode.NOT_EXIST_SUBSCRIPTION.getMessage(),
				ErrorCode.NOT_EXIST_SUBSCRIPTION.getStatus(), githubId, ghRepoId));

		subscription.toggleStar();
		subscriptionRepository.save(subscription);
	}

	private ResponseEntity<GithubOrgDto> githubGetOrgInfo(RepositoryUrlDto repositoryUrlDto, String accessToken) {
		try {
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
		} catch (
			WebClientResponseException e) {
			throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
		}
	}

	private ResponseEntity<GithubRepositoryDto> githubGetRepositoryInfo(RepositoryUrlDto repositoryUrlDto,
		String accessToken) {
		try {
			return WebClient.create()
				.get()
				.uri(
					REPOSITORY_REQUEST_URL + repositoryUrlDto.getOrgName() + "/" + repositoryUrlDto.getRepositoryName())
				.headers(headers -> {
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
					headers.setBearerAuth(accessToken);
				})
				.retrieve()
				.toEntity(GithubRepositoryDto.class)
				.block();
		} catch (WebClientResponseException e) {
			throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
		}
	}

	private void saveSubscription(User user, Repository repository) {
		if (subscriptionRepository.findByUserIdAndRepository_GhRepoId(user.getId(), repository.getGhRepoId())
			.isEmpty()) {
			Subscription newSubscription = new Subscription(user, repository);
			subscriptionRepository.save(newSubscription);
		}
	}
}
