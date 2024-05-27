package site.iris.issuefy.model.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OauthDto {

	public static final String KEY_TOKEN_TYPE = "token_type";
	public static final String KEY_ACCESS_TOKEN = "access_token";

	@JsonProperty("token_type")
	private final String tokenType;

	@JsonProperty("access_token")
	private final String accessToken;

	private final String scope;

	public static OauthDto of(String accessToken, String tokenType, String scope) {
		return new OauthDto(accessToken, tokenType, scope);
	}

	public static OauthDto fromMap(Map<String, String> map) {
		final String KEY_SCOPE = "scope";
		final String DEFAULT_TOKEN_TYPE = "bearer";

		return new OauthDto(
			map.getOrDefault(KEY_TOKEN_TYPE, DEFAULT_TOKEN_TYPE),
			map.getOrDefault(KEY_ACCESS_TOKEN, ""),
			map.getOrDefault(KEY_SCOPE, "")
		);
	}

	@Override
	public String toString() {
		return "OauthDto{" +
			"tokenType='" + tokenType + '\'' +
			", accessToken='" + accessToken + '\'' +
			", scope='" + scope + '\'' +
			'}';
	}
}
