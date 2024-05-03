package site.iris.issuefy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OauthResponse {
	private String userName;
	private String avatarURL;
	private String JWT;

	public static OauthResponse of(String userName, String avatarURL, String JWT) {
		return new OauthResponse(userName, avatarURL, JWT);
	}
}
