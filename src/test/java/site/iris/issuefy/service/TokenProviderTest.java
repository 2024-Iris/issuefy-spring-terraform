package site.iris.issuefy.service;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import site.iris.issuefy.entity.Jwt;

class TokenProviderTest {
	private static TokenProvider tokenProvider;

	@BeforeAll
	static void setUp() {
		String secretKey = "test-secret-key-test-secret-key-test-secret-key";
		tokenProvider = new TokenProvider(secretKey);
	}

	@DisplayName("토큰를 성공적으로 생성한 후 payloads를 추출한다")
	@Test
	void createToken() {
		// given
		Map<String, Object> claims = new HashMap<>();
		claims.put("testUserId", "dokkisan");

		// when
		String token = tokenProvider.createToken(claims, tokenProvider.getExpireDateAccessToken());

		// then
		assertThat(tokenProvider.getClaims(token).get("testUserId")).isEqualTo("dokkisan");
	}

	@DisplayName("JWT가 성공적으로 생성된다")
	@Test
	void createJwt() {
		// given
		Map<String, Object> claims = new HashMap<>();
		claims.put("testUserId", "dokkisan");

		// when
		Jwt jwt = tokenProvider.createJwt(claims);

		// then
		System.out.println(jwt.getAccessToken());
		System.out.println(jwt.getRefreshToken());
	}
}