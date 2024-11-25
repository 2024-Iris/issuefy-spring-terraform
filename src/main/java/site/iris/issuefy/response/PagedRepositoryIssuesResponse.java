package site.iris.issuefy.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedRepositoryIssuesResponse {
	private int currentPage;
	private int pageSize;
	private long totalElements;
	private int totalPages;
	private String repositoryName;
	private List<IssueResponse> issues;

	public static PagedRepositoryIssuesResponse of(int currentPage, int pageSize, long totalElements, int totalPages,
		String repositoryName, List<IssueResponse> issue) {
		return new PagedRepositoryIssuesResponse(currentPage, pageSize, totalElements, totalPages, repositoryName,
			issue);
	}
}
