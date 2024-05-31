package site.iris.issuefy.model.dto;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenDto {
	private String githubId;
	private Long exp;

	public static TokenDto fromClaims(Claims claims) {
		return new TokenDto((String)claims.get("githubId"), claims.getExpiration().getTime());
	}

	@Override
	public String toString() {
		return "TokenDto{" +
			"githubId='" + githubId + '\'' +
			", exp=" + exp +
			'}';
	}
}
