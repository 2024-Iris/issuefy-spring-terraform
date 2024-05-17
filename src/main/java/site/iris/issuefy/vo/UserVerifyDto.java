package site.iris.issuefy.vo;

import lombok.Getter;

@Getter
public class UserVerifyDto {
	private final boolean isValid;

	private UserVerifyDto(boolean isValid) {
		this.isValid = isValid;
	}

	public static UserVerifyDto from(boolean isValid) {
		return new UserVerifyDto(isValid);
	}
}
