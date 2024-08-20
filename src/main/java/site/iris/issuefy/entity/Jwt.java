package site.iris.issuefy.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class Jwt {
	private final String accessToken;
	private final String refreshToken;

	public static Jwt of(String accessToken, String refreshToken) {
		return new Jwt(accessToken, refreshToken);
	}
}
