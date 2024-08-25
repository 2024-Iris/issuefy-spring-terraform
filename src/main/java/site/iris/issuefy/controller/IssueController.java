package site.iris.issuefy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;
import site.iris.issuefy.service.IssueService;

@RestController
@RequiredArgsConstructor
public class IssueController {
	private final IssueService issueService;

	@GetMapping("/api/subscriptions/{org_name}/{repo_name}/issues")
	public ResponseEntity<PagedRepositoryIssuesResponse> getIssuesByRepoName(@PathVariable("org_name") String orgName,
		@PathVariable("repo_name") String repoName, @RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "updatedAt") String sort, @RequestParam(defaultValue = "desc") String order,
		@RequestAttribute String githubId) {

		int pageSize = 15;
		PagedRepositoryIssuesResponse response = issueService.getRepositoryIssues(orgName, repoName, githubId, page,
			pageSize, sort, order);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
