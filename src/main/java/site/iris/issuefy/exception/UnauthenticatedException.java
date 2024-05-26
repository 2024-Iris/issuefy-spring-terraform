package site.iris.issuefy.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UnauthenticatedException extends AuthenticationException {
	private final int statusCode;

	public static final String ACCESS_TOKEN_EXPIRED = "Unauthorized access: access token has expired";
	public static final String INVALID_HEADER = "Unauthorized access: invalid header";
	public static final String INVALID_TOKEN_TYPE = "Unauthorized access: invalid token type";

	public UnauthenticatedException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}


}
