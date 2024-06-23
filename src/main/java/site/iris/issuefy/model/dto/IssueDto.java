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
	private Long id;
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
	private long ghIssueNumber;
	private List<Label> labels;

	public static IssueDto of(Long id, String title, boolean isStarred, boolean isRead, String state,
		Date createdAt,
		Date updatedAt, Date closedAt, long ghIssueNumber, List<Label> labels) {
		return new IssueDto(id, title, isStarred, isRead, state, createdAt, updatedAt, closedAt, ghIssueNumber,
			labels);
	}
}
