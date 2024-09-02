package site.iris.issuefy.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.iris.issuefy.model.dto.CommentsDto;
import site.iris.issuefy.model.dto.IssueDetailDto;

@Data
@AllArgsConstructor
public class IssueDetailAndCommentsResponse {
	private IssueDetailDto issueDetailDto;
	private List<CommentsDto> comments;

	public static IssueDetailAndCommentsResponse of(IssueDetailDto issueDetailDto, List<CommentsDto> comments) {
		return new IssueDetailAndCommentsResponse(issueDetailDto, comments);
	}
}
