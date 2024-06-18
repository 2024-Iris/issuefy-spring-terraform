package site.iris.issuefy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueDto {
	private Long id;
	private int githubIssueNumber;
	private String title;
	private String label;

	public static IssueDto of(Long id, int githubIssueNumber, String title, String label) {
		return new IssueDto(id, githubIssueNumber, title, label);
	}
}
