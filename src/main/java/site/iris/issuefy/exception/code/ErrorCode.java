package site.iris.issuefy.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	REQUIRED_KEYS_MISSING(HttpStatus.BAD_REQUEST, "Required keys are missing"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

	private final HttpStatus status;
	private final String message;
}
