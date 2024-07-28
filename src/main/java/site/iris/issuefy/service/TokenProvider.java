package site.iris.issuefy.service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Jwt;
import site.iris.issuefy.model.dto.BlacklistedJwtDto;

@Component
@Slf4j
public class TokenProvider {

	private final RedisTemplate<String, BlacklistedJwtDto> redisTemplate;
	private Key key;

	public TokenProvider(RedisTemplate<String, BlacklistedJwtDto> redisTemplate,
		@Value("${jwt.secretKey}") String secretKey) {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
		this.redisTemplate = redisTemplate;
	}

	public Jwt createJwt(Map<String, Object> claims) {
		String accessToken = createToken(claims, getExpireDateAccessToken());
		String refreshToken = createToken(new HashMap<>(), getExpireDateRefreshToken());

		return Jwt.of(accessToken, refreshToken);
	}

	private String createToken(Map<String, Object> claims, Date expireDate) {

		return Jwts.builder()
			.claims(claims)
			.expiration(expireDate)
			.signWith(key)
			.compact();
	}

	public Claims getClaims(String token) {

		return Jwts.parser()
			.verifyWith((SecretKey)key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public boolean isValidToken(String token) {
		BlacklistedJwtDto blacklistedJwtDto = redisTemplate.opsForValue().get(token);
		if (blacklistedJwtDto != null) {
			return false;
		}
		try {
			Jws<Claims> claims = Jwts.parser()
				.verifyWith((SecretKey)key)
				.build()
				.parseSignedClaims(token);
			return claims.getPayload()
				.getExpiration()
				.after(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public void invalidateToken(String token) {
		Claims claims = getClaims(token);
		LocalDateTime expiresAt = claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime now = LocalDateTime.now();

		BlacklistedJwtDto blacklistedJwtDto = new BlacklistedJwtDto(token, now, expiresAt);

		LocalDateTime redisExpirationTime = expiresAt.plusHours(1);
		long redisExpirationSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), redisExpirationTime);
		redisTemplate.opsForValue().set(token, blacklistedJwtDto, redisExpirationSeconds, TimeUnit.SECONDS);
	}

	private Date getExpireDateAccessToken() {
		long expireTimeMils = 1000L * 60 * 60 * 8;

		return new Date(System.currentTimeMillis() + expireTimeMils);
	}

	private Date getExpireDateRefreshToken() {
		long expireTimeMils = 1000L * 60 * 60 * 24 * 60;

		return new Date(System.currentTimeMillis() + expireTimeMils);
	}
}
