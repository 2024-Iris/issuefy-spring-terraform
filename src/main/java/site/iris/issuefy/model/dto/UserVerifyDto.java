package site.iris.issuefy.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserVerifyDto {
	private final boolean isValid;

	public static UserVerifyDto from(boolean isValid) {
		return new UserVerifyDto(isValid);
	}
}
