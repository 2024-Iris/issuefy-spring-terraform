package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import site.iris.issuefy.entity.Jwt;

class TokenProviderTest {
	private static TokenProvider tokenProvider;

	@BeforeAll
	static void setUp() {
		String secretKey = "test-secret-key-test-secret-key-test-secret-key";
		tokenProvider = new TokenProvider(secretKey);
	}

	@DisplayName("JWT가 성공적으로 생성된다")
	@Test
	void createJwt() {
		// given
		Map<String, Object> claims = new HashMap<>();
		claims.put("githubId", "dokkisan");

		// when
		Jwt jwt = tokenProvider.createJwt(claims);

		// then
		assertNotNull(jwt);
		assertNotNull(jwt.getAccessToken());

		Claims accessTokenClaims = tokenProvider.getClaims(jwt.getAccessToken());
		assertEquals("dokkisan", accessTokenClaims.get("githubId"));
		assertTrue(tokenProvider.isValidToken(jwt.getAccessToken()));
	}

	@DisplayName("유효하지 않은 토큰은 false를 반환한다")
	@Test
	void isValidJwt() {
		// given
		String invalidToken = "invalid.token.string";

		// when
		boolean isValid = tokenProvider.isValidToken(invalidToken);

		// then
		assertFalse(isValid);
	}
}