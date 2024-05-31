package site.iris.issuefy.response;

import site.iris.issuefy.model.vo.OrgRecord;

public record SubscribeResponse(OrgRecord org) {
	public static SubscribeResponse from(OrgRecord org) {
		return new SubscribeResponse(org);
	}

	@Override
	public String toString() {
		return "SubscribeResponse{" +
			"org=" + org +
			'}';
	}
}
