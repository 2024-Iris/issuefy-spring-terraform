package site.iris.issuefy.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.dto.RepositoryResponse;
import site.iris.issuefy.service.RepositoryService;
import site.iris.issuefy.vo.RepositoryVO;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
public class RepositoryController {
	private final RepositoryService repositoryService;

	@GetMapping
	public ResponseEntity<List<RepositoryResponse>> getSubscribedRepositories() {
		List<RepositoryResponse> repositoryResponses = repositoryService.getSubscribedRepositories();

		return ResponseEntity.ok(repositoryResponses);
	}

	@PostMapping
	public ResponseEntity<RepositoryResponse> create(@RequestBody RepositoryVO repositoryVO) {
		RepositoryResponse repositoryResponse = RepositoryResponse.from(repositoryVO);

		return ResponseEntity.created(URI.create("/repositories/" + repositoryResponse.getId())).body(
			repositoryResponse);
	}
}
