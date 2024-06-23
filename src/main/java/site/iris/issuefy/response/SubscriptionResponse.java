package site.iris.issuefy.response;

import site.iris.issuefy.model.vo.OrgRecord;

public record SubscriptionResponse(OrgRecord org) {
	public static SubscriptionResponse from(OrgRecord org) {
		return new SubscriptionResponse(org);
	}

	@Override
	public String toString() {
		return "SubscriptionResponse{" +
			"org=" + org +
			'}';
	}
}
