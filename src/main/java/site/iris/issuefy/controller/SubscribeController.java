package site.iris.issuefy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.SubscribeResponse;
import site.iris.issuefy.service.RepositoryService;

@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
@Slf4j
public class SubscribeController {
	private static final int TOKEN_INDEX = 1;
	private final RepositoryService repositoryService;

	@GetMapping
	public ResponseEntity<List<SubscribeResponse>> getSubscribedRepositories(
		@RequestHeader("Authorization") String token) {
		String[] tokens = token.split(" ");
		return ResponseEntity.ok(repositoryService.getSubscribedRepositories(tokens[TOKEN_INDEX]));
	}

	// @PostMapping
	// public ResponseEntity<SubscribeResponse> create(@RequestBody RepositoryVO repositoryVO) {
	// 	SubscribeResponse repositoryResponse = SubscribeResponse.from(repositoryVO);
	//
	// 	return ResponseEntity.created(URI.create("/repositories/" + repositoryResponse.getId())).body(
	// 		repositoryResponse);
	// }
}
