package site.iris.issuefy.exception;

public class InvalidUrlException extends ValidException {

	public static final String INVALID_URL = "Invalid repository URL";

	public InvalidUrlException(String message) {
		super(message);
	}
}
