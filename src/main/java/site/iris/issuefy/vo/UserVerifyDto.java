package site.iris.issuefy.vo;

import lombok.RequiredArgsConstructor;

public class UserVerifyDto {
	private boolean isValid;

	private UserVerifyDto(boolean isValid) {
		this.isValid = isValid;
	}

	public static UserVerifyDto from(boolean isValid) {
		return new UserVerifyDto(isValid);
	}
}
