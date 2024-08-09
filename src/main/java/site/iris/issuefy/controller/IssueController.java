package site.iris.issuefy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.response.RepositoryIssuesResponse;
import site.iris.issuefy.service.IssueService;

@RestController
@RequiredArgsConstructor
public class IssueController {
	private final IssueService issueService;

	@GetMapping("/api/subscriptions/{org_name}/{repo_name}/issues")
	public ResponseEntity<RepositoryIssuesResponse> getIssuesByRepoName(@PathVariable("org_name") String orgName,
		@PathVariable("repo_name") String repoName,
		@RequestAttribute String githubId) {
		RepositoryIssuesResponse response = issueService.initializeIssueSubscription(orgName, repoName, githubId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
