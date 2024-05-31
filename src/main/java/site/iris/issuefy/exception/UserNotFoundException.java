package site.iris.issuefy.exception;

public class UserNotFoundException extends EntityException {
	public static final String USER_ID = "GithubId : ";
	public static final String USER_NOT_FOUND = "User not found";

	public UserNotFoundException(String githubId) {
		super(USER_ID + githubId + USER_NOT_FOUND);
	}
}
