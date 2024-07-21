package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import io.jsonwebtoken.Claims;
import site.iris.issuefy.entity.Jwt;
import site.iris.issuefy.model.dto.BlacklistedJwtDto;

class TokenProviderTest {

    private TokenProvider tokenProvider;

    @Mock
    private RedisTemplate<String, BlacklistedJwtDto> redisTemplate;

    @Mock
    private ValueOperations<String, BlacklistedJwtDto> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String secretKey = "test-secret-key-test-secret-key-test-secret-key";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenProvider = new TokenProvider(redisTemplate, secretKey);
    }

    @DisplayName("Jwt가 성공적으로 생성된다.")
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

    @DisplayName("유효하지 않은 토큰은 false를 반환한다.")
    @Test
    void isValidJwt() {
        // given
        String invalidToken = "invalid.token.string";

        // when
        boolean isValid = tokenProvider.isValidToken(invalidToken);

        // then
        assertFalse(isValid);
    }

    @DisplayName("토큰 무효화 메서드가 올바르게 호출된다")
    @Test
    void invalidateToken() {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put("githubId", "dokkisan");

        Jwt jwt = tokenProvider.createJwt(claims);
        String token = jwt.getAccessToken();

        // when
        tokenProvider.invalidateToken(token);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(
            eq(token),
            any(BlacklistedJwtDto.class),
            anyLong(),
            eq(TimeUnit.SECONDS)
        );
    }

    @DisplayName("블랙리스트에 있는 토큰은 유효하지 않다고 판단한다.")
    @Test
    void isValidToken_blacklisted() {
        // given
        String token = "blacklisted.token";
        when(valueOperations.get(token)).thenReturn(new BlacklistedJwtDto(token, LocalDateTime.now(), LocalDateTime.now().plusHours(1)));

        // when
        boolean isValid = tokenProvider.isValidToken(token);

        // then
        assertFalse(isValid);
    }
}