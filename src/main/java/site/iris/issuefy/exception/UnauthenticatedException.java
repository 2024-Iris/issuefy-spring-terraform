package site.iris.issuefy.exception;

public class UnauthenticatedException extends AuthenticationException {
	public static final String MESSAGE = "Unauthorized access: no authentication credentials provided.";

	public UnauthenticatedException() {
		super(MESSAGE);
	}
}
