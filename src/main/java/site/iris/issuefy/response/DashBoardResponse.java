package site.iris.issuefy.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashBoardResponse {
	private String rank;
	private String visitCount;
	private String addRepositoryCount;

	public static DashBoardResponse of(String rank, String visitCount, String addRepositoryCount) {
		return new DashBoardResponse(rank, visitCount, addRepositoryCount);
	}
}
