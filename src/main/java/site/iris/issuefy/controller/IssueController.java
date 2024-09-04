package site.iris.issuefy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.response.IssueDetailAndCommentsResponse;
import site.iris.issuefy.response.PagedRepositoryIssuesResponse;
import site.iris.issuefy.response.StarRepositoryIssuesResponse;
import site.iris.issuefy.service.IssueService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class IssueController {
	private final IssueService issueService;

	@GetMapping("/{org_name}/{repo_name}/issues")
	public ResponseEntity<PagedRepositoryIssuesResponse> getIssuesByRepoName(@PathVariable("org_name") String orgName,
		@PathVariable("repo_name") String repoName, @RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "updatedAt") String sort, @RequestParam(defaultValue = "desc") String order,
		@RequestAttribute String githubId) {

		int pageSize = 15;
		PagedRepositoryIssuesResponse response = issueService.getRepositoryIssues(orgName, repoName, githubId, page,
			pageSize, sort, order);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{org_name}/{repo_name}/issues/{issue_number}")
	public ResponseEntity<IssueDetailAndCommentsResponse> getIssueDetails(@PathVariable("org_name") String orgName,
		@PathVariable("repo_name") String repoName,
		@PathVariable("issue_number") String issueNumber, @RequestAttribute String githubId) {
		IssueDetailAndCommentsResponse issueDetailAndCommentResponse = issueService.getIssueDetailAndComments(orgName,
			repoName, issueNumber, githubId);
		return ResponseEntity.status(HttpStatus.OK).body(issueDetailAndCommentResponse);
	}

	@GetMapping("/issue_star")
	public ResponseEntity<StarRepositoryIssuesResponse> getIssueStar(@RequestAttribute String githubId) {
		StarRepositoryIssuesResponse response = issueService.getStaredRepositoryIssuesResponse(githubId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/issue_star/{gh_issue_id}")
	public ResponseEntity<Void> updateIssueStar(@RequestAttribute String githubId,
		@PathVariable("gh_issue_id") Long issueId) {
		issueService.toggleIssueStar(githubId, issueId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
