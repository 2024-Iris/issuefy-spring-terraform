package site.iris.issuefy.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssueStarResponse {
	private Long id;
	private String orgName;
	private String repositoryName;
	private Long githubIssueId;
	private String state;
	private String title;
	private List<LabelResponse> labels;
	private boolean read;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime closedAt;
	private boolean starred;

	public static IssueStarResponse of(
		Long id,
		String orgName,
		String repositoryName,
		Long githubIssueId,
		String state,
		String title,
		List<LabelResponse> labels,
		boolean read,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime closedAt,
		boolean starred) {
		return new IssueStarResponse(
			id,
			orgName,
			repositoryName,
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
