package site.iris.issuefy.response;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IssueResponse {
	private Long id;
	private Long githubIssueId;
	private String state;
	private String title;
	private List<LabelResponse> labels;
	private boolean isRead;
	private boolean isStarred;
	private Date createdAt;
	private Date updatedAt;
	private Date closedAt;
}
