package site.iris.issuefy.eums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LokiQuery {
	NUMBER_OF_WEEKLY_VISIT(
		"sum(count_over_time({job=\"issuefylog\"} |= \"[%s]\" |= \"Response: 200 GET /api/login - Method: login \" [7d]))"),
	NUMBER_OF_WEEKLY_REPOSITORY_ADDED(
		"sum(count_over_time({job=\"issuefylog\"} |= \"[%s]\" |= \"Request: POST /api/subscriptions - Method: addRepository \" [7d]))");

	private final String query;
}
