package site.iris.issuefy.vo;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OauthDto {

	private final String access_token;
	private final String scope;
	private final String token_type;

	public static OauthDto of(String access_token, String scope, String token_type) {
		return new OauthDto(access_token, scope, token_type);
	}

	public static OauthDto fromMap(Map<String, String> map) {
		return new OauthDto(
			map.getOrDefault("access_token", ""),
			map.getOrDefault("scope", ""),
			map.getOrDefault("token_type", "bearer")  // Default token type to 'bearer' if not provided
		);
	}

	@Override
	public String toString() {
		return "GithubOauthDto{" +
			"access_token='" + access_token + '\'' +
			", scope='" + scope + '\'' +
			", token_type='" + token_type + '\'' +
			'}';
	}
}
