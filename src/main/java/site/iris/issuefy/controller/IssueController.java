package site.iris.issuefy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.dto.IssueResponse;
import site.iris.issuefy.service.IssueService;

@RestController
@RequiredArgsConstructor
public class IssueController {
	private final IssueService issueService;

	@GetMapping("/{repoName}/issues")
	public ResponseEntity<List<IssueResponse>> getIssuesByRepoName(@PathVariable("repoName") String repoName) {
		List<IssueResponse> issues = issueService.getIssuesByRepoName(repoName);

		return ResponseEntity.ok(issues);
	}
}
