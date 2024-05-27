package site.iris.issuefy.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.iris.issuefy.entity.Jwt;

@Data
@AllArgsConstructor
public class OauthResponse {
	private String userName;
	private String avatarURL;
	private Jwt jwt;

	public static OauthResponse of(String userName, String avatarURL, Jwt jwt) {
		return new OauthResponse(userName, avatarURL, jwt);
	}
}
