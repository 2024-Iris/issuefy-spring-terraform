package site.iris.issuefy.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	REQUIRED_KEYS_MISSING(HttpStatus.BAD_REQUEST, "Required keys are missing"),
	NOT_EXIST_SUBSCRIPTION(HttpStatus.NOT_FOUND, "Subscription does not exist"),
	NOT_EXIST_REPOSITORY(HttpStatus.NOT_FOUND, "Repository does not exist"),
	NOT_EXIST_ISSUE(HttpStatus.NOT_FOUND, "Issue does not exist"),
	NOT_EXIST_USER(HttpStatus.NOT_FOUND, "User does not exist"),
	USER_SUBSCRIPTIONS_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "User subscriptions page not found"),
	USER_STARRED_SUBSCRIPTIONS_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "User starred subscriptions page not found"),
	FAILED_INIT_CONNECTION(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize sse connection"),
	FAILED_SENDING_MESSAGE(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send sse message"),
	UNKNOWN_SSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown sse error"),
	ORG_BODY_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "Org info body is empty"),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access token has expired"),
	INVALID_HEADER(HttpStatus.UNAUTHORIZED, "Invalid header"),
	INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "Invalid token type"),
	INVALID_REPOSITORY_URL(HttpStatus.BAD_REQUEST, "Invalid Repository URL"),
	REPOSITORY_BODY_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "Repository info body is empty"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

	private final HttpStatus status;
	private final String message;
}
