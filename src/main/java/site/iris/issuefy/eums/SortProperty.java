package site.iris.issuefy.eums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortProperty {
	REPOSITORY_NAME("repository.name"),
	ORG_NAME("repository.org.name"),
	LATEST_UPDATE("repository.latestUpdateAt");

	private final String value;

	public static String fromString(String text) {
		return Arrays.stream(values())
			.filter(sortProperty -> sortProperty.name().replace("_", "").equalsIgnoreCase(text))
			.findFirst()
			.map(SortProperty::getValue)
			.orElse(LATEST_UPDATE.getValue());
	}
}