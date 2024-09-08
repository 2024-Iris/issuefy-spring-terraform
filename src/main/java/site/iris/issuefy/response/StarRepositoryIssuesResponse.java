package site.iris.issuefy.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StarRepositoryIssuesResponse {
	private List<IssueStarResponse> issues;

	public static StarRepositoryIssuesResponse of(List<IssueStarResponse> issue) {
		return new StarRepositoryIssuesResponse(issue);
	}
}
