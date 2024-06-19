package site.iris.issuefy.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueDto {
	private Long id;
	private int githubIssueNumber;
	private String title;
	private List<LabelDto> labels;

	public static IssueDto of(Long id, int githubIssueNumber, String title, List<LabelDto> labels) {
		return new IssueDto(id, githubIssueNumber, title, labels);
	}
}
