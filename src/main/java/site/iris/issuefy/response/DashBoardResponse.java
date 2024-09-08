package site.iris.issuefy.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashBoardResponse {
	private LocalDate startDate;
	private LocalDate endDate;
	private String rank;
	private String visitCount;
	private String addRepositoryCount;

	public static DashBoardResponse of(LocalDate startDate, LocalDate endDate, String rank, String visitCount,
		String addRepositoryCount) {
		return new DashBoardResponse(startDate, endDate, rank, visitCount, addRepositoryCount);
	}
}
