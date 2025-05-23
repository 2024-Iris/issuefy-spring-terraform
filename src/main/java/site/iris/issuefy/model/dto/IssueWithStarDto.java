package site.iris.issuefy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.iris.issuefy.entity.Issue;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueWithStarDto {
	private Issue issue;
	private boolean isStarred;
	private String orgName;
	private String repositoryName;
}
