package site.iris.issuefy.controller;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.model.vo.RepositoryRecord;
import site.iris.issuefy.response.SubscriptionResponse;
import site.iris.issuefy.service.GithubTokenService;
import site.iris.issuefy.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

	private final SubscriptionService subscriptionService;
	private final GithubTokenService githubTokenService;

	@GetMapping
	public ResponseEntity<List<SubscriptionResponse>> getSubscribedRepositories(
		@RequestAttribute("githubId") String githubId) {
		logRequest(githubId, "Request SubscribedRepositories");
		List<SubscriptionResponse> subscriptionResponses = subscriptionService.getSubscribedRepositories(githubId);
		logResponse(githubId, subscriptionResponses);
		return ResponseEntity.ok(subscriptionResponses);
	}

	@PostMapping
	public ResponseEntity<String> addRepository(@RequestAttribute String githubId,
		@RequestBody RepositoryRecord RepositoryRecord) {
		logRequest(githubId, "Request AddRepository");
		checkRepositoryExistence(githubId, RepositoryRecord);
		RepositoryUrlDto repositoryUrlDto = RepositoryUrlDto.of(RepositoryRecord.repositoryUrl(), githubId);
		subscriptionService.addSubscribeRepository(repositoryUrlDto, githubId);
		logResponse(githubId, RepositoryRecord.repositoryUrl());
		return ResponseEntity.created(URI.create(RepositoryRecord.repositoryUrl())).build();
	}

	@DeleteMapping("/{gh_repo_id}")
	public ResponseEntity<Void> unsubscribeRepository(@RequestAttribute String githubId,
		@PathVariable("gh_repo_id") Long ghRepoId) {
		logRequest(githubId, "Request UnsubscribeRepository for RepoId: " + ghRepoId);
		subscriptionService.unsubscribeRepository(ghRepoId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("star/{gh_repo_id}")
	public ResponseEntity<Void> starRepository(@RequestAttribute String githubId,
		@PathVariable("gh_repo_id") Long ghRepoId) {
		logRequest(githubId, "Request StarRepository for RepoId: " + ghRepoId);
		subscriptionService.toggleRepositoryStar(githubId, ghRepoId);
		return ResponseEntity.noContent().build();
	}

	private void checkRepositoryExistence(String githubId, RepositoryRecord RepositoryRecord) {
		String accessToken = githubTokenService.findAccessToken(githubId);
		logRequest(githubId, "Request Github API, Repository Url : " + RepositoryRecord.repositoryUrl());
		WebClient.create()
			.get()
			.uri(RepositoryRecord.repositoryUrl())
			.headers(headers -> {
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setBearerAuth(accessToken);
			})
			.retrieve()
			.toBodilessEntity()
			.block();
	}

	private void logRequest(String githubId, String message) {
		log.info("GitHub ID: {} - {}", githubId, message);
	}

	private void logResponse(String githubId, Object response) {
		log.info("{} : Response {}", githubId, response);
	}
}