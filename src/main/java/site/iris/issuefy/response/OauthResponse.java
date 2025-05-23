package site.iris.issuefy.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import site.iris.issuefy.entity.Jwt;

@Data
@AllArgsConstructor
public class OauthResponse {
	private String userName;
	private String userEmail;
	private String avatarURL;
	private boolean alertStatus;
	private Jwt jwt;

	public static OauthResponse of(String userName, String userEmail, String avatarURL, boolean alertStatus, Jwt jwt) {
		return new OauthResponse(userName, userEmail, avatarURL, alertStatus, jwt);
	}
}
