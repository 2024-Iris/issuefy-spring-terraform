package site.iris.issuefy.model.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.iris.issuefy.entity.Label;

@Data
@AllArgsConstructor
public class IssueDto {
	@JsonProperty("id")
	private Long ghIssueId;

	private String title;
	private boolean isStarred;
	private boolean isRead;
	private String state;

	@JsonProperty("created_at")
	private Date createdAt;

	@JsonProperty("updated_at")
	private Date updatedAt;

	@JsonProperty("closed_at")
	private Date closedAt;

	private List<Label> labels;

	public static IssueDto of(Long ghIssueId, String title, boolean isStarred, boolean isRead, String state,
		Date createdAt,
		Date updatedAt, Date closedAt, List<Label> labels) {
		return new IssueDto(ghIssueId, title, isStarred, isRead, state, createdAt, updatedAt, closedAt,
			labels);
	}
}
