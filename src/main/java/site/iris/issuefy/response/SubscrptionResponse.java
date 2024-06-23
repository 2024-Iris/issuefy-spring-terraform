package site.iris.issuefy.response;

import site.iris.issuefy.model.vo.OrgRecord;

public record SubscrptionResponse(OrgRecord org) {
	public static SubscrptionResponse from(OrgRecord org) {
		return new SubscrptionResponse(org);
	}

	@Override
	public String toString() {
		return "SubscrptionResponse{" +
			"org=" + org +
			'}';
	}
}
