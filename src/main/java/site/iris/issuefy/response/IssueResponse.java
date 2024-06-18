package site.iris.issuefy.response;

import java.util.List;

import lombok.Data;

@Data
public class IssueResponse<IssueDto> {
	private List<IssueDto> issues;

	public IssueResponse(List<IssueDto> issues) {
		this.issues = issues;
	}
}
