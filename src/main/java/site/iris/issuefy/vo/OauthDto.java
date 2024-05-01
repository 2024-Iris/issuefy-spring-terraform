package site.iris.issuefy.vo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OauthDto {

	private final String access_token;
	private final String scope;
	private final String token_type;

	public static OauthDto of(String access_token, String scope, String token_type) {
		return new OauthDto(access_token, scope, token_type);
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
