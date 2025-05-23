package site.iris.issuefy.controller;

import java.net.URI;
import java.util.Collections;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.model.vo.RepositoryRecord;
import site.iris.issuefy.response.PagedSubscriptionResponse;
import site.iris.issuefy.service.GithubTokenService;
import site.iris.issuefy.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;
	private final GithubTokenService githubTokenService;

	@GetMapping
	public ResponseEntity<PagedSubscriptionResponse> getSubscribedRepositories(
		@RequestAttribute("githubId") String githubId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "latestUpdateAt") String sort,
		@RequestParam(defaultValue = "desc") String order,
		@RequestParam(defaultValue = "false") boolean starred) {

		int pageSize = 15;
		PagedSubscriptionResponse response = subscriptionService.getSubscribedRepositories(githubId, page, pageSize,
			sort, order, starred);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<String> addRepository(@RequestAttribute String githubId,
		@RequestBody RepositoryRecord RepositoryRecord) {
		checkRepositoryExistence(githubId, RepositoryRecord);
		RepositoryUrlDto repositoryUrlDto = RepositoryUrlDto.of(RepositoryRecord.repositoryUrl(), githubId);
		subscriptionService.addSubscribeRepository(repositoryUrlDto, githubId);
		return ResponseEntity.created(URI.create(RepositoryRecord.repositoryUrl())).build();
	}

	@DeleteMapping("/{gh_repo_id}")
	public ResponseEntity<Void> unsubscribeRepository(@RequestAttribute String githubId,
		@PathVariable("gh_repo_id") Long ghRepoId) {
		subscriptionService.unsubscribeRepository(ghRepoId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("star/{gh_repo_id}")
	public ResponseEntity<Void> starRepository(@RequestAttribute String githubId,
		@PathVariable("gh_repo_id") Long ghRepoId) {
		subscriptionService.toggleRepositoryStar(githubId, ghRepoId);
		return ResponseEntity.noContent().build();
	}

	private void checkRepositoryExistence(String githubId, RepositoryRecord RepositoryRecord) {
		String accessToken = githubTokenService.findAccessToken(githubId);
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
}