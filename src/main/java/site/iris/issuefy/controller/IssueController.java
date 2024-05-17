package site.iris.issuefy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.dto.IssueResponse;
import site.iris.issuefy.service.IssueService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class IssueController {
	private final IssueService issueService;

	@GetMapping("/api/{repoName}/issues")
	public ResponseEntity<String> getIssuesByRepoName(@PathVariable("repoName") String repoName) {
		log.info("getIssuesByRepoName: {}", repoName);
		String answer = issueService.getIssuesByRepoName(repoName);
		return ResponseEntity.ok(answer);
	}
}
