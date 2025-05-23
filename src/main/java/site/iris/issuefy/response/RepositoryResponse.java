package site.iris.issuefy.response;

import java.util.List;

import lombok.Data;

@Data
public class RepositoryResponse {
	private String repositoryName;
	private List<IssueResponse> issues;
}
