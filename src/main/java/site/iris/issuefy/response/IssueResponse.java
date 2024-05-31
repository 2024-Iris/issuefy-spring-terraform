package site.iris.issuefy.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueResponse {
	private Long id;
	private int githubIssueNumber;
	private String title;
	private String label;

	public static IssueResponse of(Long id, int githubIssueNumber, String title, String label) {
		return new IssueResponse(id, githubIssueNumber, title, label);
	}
}
