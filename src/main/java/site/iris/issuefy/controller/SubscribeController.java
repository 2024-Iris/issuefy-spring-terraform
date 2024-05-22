package site.iris.issuefy.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.SubscribeResponse;
import site.iris.issuefy.service.RepositoryService;
import site.iris.issuefy.vo.OrgRecord;

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

	@PostMapping
	public ResponseEntity<String> create(@RequestBody String url) {

		long id = 10L;
		String org = "testOrg";
		String repository = "testRepository";

		return ResponseEntity.created(URI.create("/subscribe/" + id)).body(
			repository);
	}
}
