package site.iris.issuefy.response;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class IssueResponse {
	private int id;
	private int githubIssueId;
	private String state;
	private String title;
	private List<LabelResponse> labels;
	private boolean isRead;
	private boolean isStarred;
	private Date createdAt;
	private Date updatedAt;
	private Date closedAt;

}
