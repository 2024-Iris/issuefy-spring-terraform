package site.iris.issuefy.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.iris.issuefy.exception.code.ErrorCode;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
	private int status;
	private String message;

	private ErrorResponse(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.getStatus(), message);
	}
}
