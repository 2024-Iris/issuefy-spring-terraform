package site.iris.issuefy.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueResponse {
	private Long id;
	private Long githubIssueId;
	private String state;
	private String title;
	private List<LabelResponse> labels;
	private boolean read;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime closedAt;
	private boolean starred;

	public static IssueResponse of(
		Long id,
		Long githubIssueId,
		String state,
		String title,
		List<LabelResponse> labels,
		boolean read,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime closedAt,
		boolean starred) {
		return new IssueResponse(
			id,
			githubIssueId,
			state,
			title,
			labels,
			read,
			createdAt,
			updatedAt,
			closedAt,
			starred
		);
	}

}
