package site.iris.issuefy.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
	public ResponseEntity<String> addRepository(@RequestBody RepositoryUrlVo repositoryUrlVo) {
		try {
			validateUrl(repositoryUrlVo);
			// TODO 필터에서 넘어온 유저이름 적용하기
			RepositoryUrlDto repositoryUrlDto = RepositoryUrlDto.of(repositoryUrlVo.repositoryUrl(), "lvalentine6");
			subscribeService.addSubscribeRepository(repositoryUrlDto);
		} catch (WebClientException webClientException) {
			log.warn(NOT_EXIST_MESSAGE + ": {}", webClientException.getMessage());
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().build();
	}

	public void validateUrl(RepositoryUrlVo repositoryUrlVo) {
		// TODO 스프링 필터에서 유저의 id값을 넘겨주고 받아오기
		// TODO 로깅 전략 적용
		// TODO DB 저장 및 반환
		String accessToken = githubTokenRepository.findAccessToken("lvalentine6");
		ResponseEntity<Void> response = WebClient.create()
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
