package site.iris.issuefy.dto;

import lombok.Data;

@Data
public class IssueResponse {
	private Long id;
	private int githubIssueNumber;
	private String title;
	private String label;

	public IssueResponse(Long id, int githubIssueNumber, String title, String label) {
		this.id = id;
		this.githubIssueNumber = githubIssueNumber;
		this.title = title;
		this.label = label;
	}
}
