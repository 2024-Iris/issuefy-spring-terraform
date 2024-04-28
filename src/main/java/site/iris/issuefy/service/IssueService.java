package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import site.iris.issuefy.dto.IssueResponse;

@Service
public class IssueService {

	public List<IssueResponse> getIssuesByRepoName(String repoName) {
		IssueResponse issueResponse = IssueResponse.of(1L, 1, "CI/CD", "deploy");
		List<IssueResponse> issueResponses = new ArrayList<>();
		issueResponses.add(issueResponse);

		return issueResponses;
	}
}
