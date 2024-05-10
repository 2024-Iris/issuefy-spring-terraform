package site.iris.issuefy.entity;

import lombok.Getter;

@Getter
public class Jwt {
	private final String accessToken;
	private final String refreshToken;

	private Jwt(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public static Jwt of(String accessToken, String refreshToken) {
		return new Jwt(accessToken, refreshToken);
	}
}
