package site.iris.issuefy.dto;

import lombok.Data;

@Data
public class IssueResponse {
	private Long id;
	private int githubIssueNumber;
	private String title;
	private String label;

	private IssueResponse(Long id, int githubIssueNumber, String title, String label) {
		this.id = id;
		this.githubIssueNumber = githubIssueNumber;
		this.title = title;
		this.label = label;
	}

	public static IssueResponse of(Long id, int githubIssueNumber, String title, String label) {
		return new IssueResponse(id, githubIssueNumber, title, label);
	}
}
