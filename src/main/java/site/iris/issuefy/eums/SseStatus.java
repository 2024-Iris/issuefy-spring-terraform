package site.iris.issuefy.eums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SseStatus {
	INIT_CONNECTION("initial", "connected successfully."),
	CLOSE_CONNECTION("error", "connection was terminated successfully.");

	private final String eventName;
	private final String data;
}
