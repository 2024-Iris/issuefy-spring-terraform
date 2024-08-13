package site.iris.issuefy.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.iris.issuefy.model.dto.SubscriptionListDto;

@AllArgsConstructor
@Getter
public class PagedSubscriptionResponse {
	private int currentPage;
	private int pageSize;
	private long totalElements;
	private int totalPages;
	private List<SubscriptionListDto> subscriptionListDtos;

	public static PagedSubscriptionResponse of(
		int currentPage,
		int pageSize,
		long totalElements,
		int totalPages,
		List<SubscriptionListDto> subscriptionListDtos
	) {
		return new PagedSubscriptionResponse(currentPage, pageSize, totalElements, totalPages, subscriptionListDtos);
	}
}
