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
	private boolean isRead;
	private boolean isStarred;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime closedAt;

	public static IssueResponse of(
		Long id,
		Long githubIssueId,
		String state,
		String title,
		List<LabelResponse> labels,
		boolean isRead,
		boolean isStarred,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime closedAt) {
		return new IssueResponse(
			id,
			githubIssueId,
			state,
			title,
			labels,
			isRead,
			isStarred,
			createdAt,
			updatedAt,
			closedAt
		);
	}

}
