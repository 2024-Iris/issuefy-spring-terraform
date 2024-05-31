package site.iris.issuefy.controller;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.RepositoryUrlDto;
import site.iris.issuefy.model.vo.RepositoryRecord;
import site.iris.issuefy.response.SubscribeResponse;
import site.iris.issuefy.service.GithubTokenService;
import site.iris.issuefy.service.SubscribeService;

@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
@Slf4j
public class SubscribeController {
	private static final String NOT_EXIST_REPOSITORY_MESSAGE = "Repository does not exist";

	private final SubscribeService subscribeService;
	private final GithubTokenService githubTokenService;

	@GetMapping
	public ResponseEntity<List<SubscribeResponse>> getSubscribedRepositories(
		@RequestAttribute("githubId") String githubId) {
		logRequest(githubId, "Request SubscribedRepositories");
		List<SubscribeResponse> subscribeResponse = subscribeService.getSubscribedRepositories(githubId);
		logResponse(githubId, subscribeResponse);
		return ResponseEntity.ok(subscribeResponse);
	}

	@PostMapping
	public ResponseEntity<String> addRepository(@RequestAttribute String githubId,
		@RequestBody RepositoryRecord RepositoryRecord) {
		logRequest(githubId, "Request AddRepository");
		try {
			checkRepositoryExistence(githubId, RepositoryRecord);
			RepositoryUrlDto repositoryUrlDto = RepositoryUrlDto.of(RepositoryRecord.repositoryUrl(), githubId);
			subscribeService.addSubscribeRepository(repositoryUrlDto, githubId);
		} catch (WebClientException e) {
			return handleWebClientException(e);
		}
		logResponse(githubId, RepositoryRecord.repositoryUrl());
		return ResponseEntity.created(URI.create(RepositoryRecord.repositoryUrl())).build();
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

	private ResponseEntity<String> handleWebClientException(WebClientException e) {
		log.warn(NOT_EXIST_REPOSITORY_MESSAGE + ": {}", e.getMessage());
		return ResponseEntity.notFound().build();
	}

	private void logRequest(String githubId, String message) {
		log.info("{} : {}", githubId, message);
	}

	private void logResponse(String githubId, Object response) {
		log.info("{} : Response {}", githubId, response);
	}
}