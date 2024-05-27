package site.iris.issuefy.dto;

import site.iris.issuefy.vo.OrgRecord;

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
