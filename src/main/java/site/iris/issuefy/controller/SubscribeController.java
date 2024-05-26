package site.iris.issuefy.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.SubscribeResponse;
import site.iris.issuefy.repository.GithubTokenRepository;
import site.iris.issuefy.service.SubscribeService;
import site.iris.issuefy.vo.RepositoryUrlDto;
import site.iris.issuefy.vo.RepositoryUrlVo;

@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
@Slf4j
public class SubscribeController {
	private static final int TOKEN_INDEX = 1;
	private final SubscribeService subscribeService;
	private final GithubTokenRepository githubTokenRepository;
	private static final String NOT_EXIST_MESSAGE = "Not Exist Repository";

	@GetMapping
	public ResponseEntity<List<SubscribeResponse>> getSubscribedRepositories(
		@RequestHeader("Authorization") String token) {
		String[] tokens = token.split(" ");
		return ResponseEntity.ok(subscribeService.getSubscribedRepositories(tokens[TOKEN_INDEX]));
	}

	@PostMapping
	public ResponseEntity<String> addRepository(@RequestAttribute String githubId,
		@RequestBody RepositoryUrlVo repositoryUrlVo) {
		try {
			validateUrl(githubId, repositoryUrlVo);
			RepositoryUrlDto repositoryUrlDto = RepositoryUrlDto.of(repositoryUrlVo.repositoryUrl(), githubId);
			subscribeService.addSubscribeRepository(repositoryUrlDto);
		} catch (WebClientException webClientException) {
			log.warn(NOT_EXIST_MESSAGE + ": {}", webClientException.getMessage());
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().build();
	}

	public void validateUrl(String githubId, RepositoryUrlVo repositoryUrlVo) {
		// TODO 로깅 전략 적용
		String accessToken = githubTokenRepository.findAccessToken(githubId);
		WebClient.create()
			.get()
			.uri(repositoryUrlVo.repositoryUrl())
			.headers(headers -> {
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setBearerAuth(accessToken);
			})
			.retrieve()
			.toBodilessEntity()
			.block();
	}
}
