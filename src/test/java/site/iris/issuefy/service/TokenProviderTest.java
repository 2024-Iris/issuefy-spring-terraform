package site.iris.issuefy.service;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenProviderTest {
	private static TokenProvider tokenProvider;

	@BeforeAll
	static void setUp() {
		String secretKey = "test-secret-key-test-secret-key-test-secret-key";
		tokenProvider = new TokenProvider(secretKey);
	}

	@DisplayName("토큰를 성공적으로 생성한다.")
	@Test
	void createToken() {
		// given
		Map<String, Object> claims = new HashMap<>();
		claims.put("testUserId", "testUser");

		// when
		String token = tokenProvider.createToken(claims);

		// then
		assertThat(token).isNotNull().isNotEmpty().isNotBlank();
	}
}