package site.iris.issuefy.model.vo;

import java.util.List;

import site.iris.issuefy.response.SubscriptionResponse;

public record PagedSubscriptionResponse(
	int currentPage,
	int pageSize,
	long totalElements,
	int totalPages,
	List<SubscriptionResponse> subscriptionResponses
) {
	public static PagedSubscriptionResponse of(
		List<SubscriptionResponse> organizations,
		int currentPage,
		int pageSize,
		long totalElements,
		int totalPages
	) {
		return new PagedSubscriptionResponse(currentPage, pageSize, totalElements, totalPages, organizations);
	}

	@Override
	public String toString() {
		return "PagedSubscriptionResponse{" +
			"subscriptionResponses=" + subscriptionResponses +
			", currentPage=" + currentPage +
			", pageSize=" + pageSize +
			", totalElements=" + totalElements +
			", totalPages=" + totalPages +
			'}';
	}
}