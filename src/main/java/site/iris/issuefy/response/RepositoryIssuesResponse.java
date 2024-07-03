package site.iris.issuefy.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepositoryIssuesResponse {
	private String repositoryName;
	private List<IssueResponse> issues;
}
