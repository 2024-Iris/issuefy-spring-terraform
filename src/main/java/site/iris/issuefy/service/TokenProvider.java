package site.iris.issuefy.service;

import java.security.Key;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Jwt;

@Component
@Slf4j
public class TokenProvider {

    private static final ZoneOffset UTC_ZONE = ZoneOffset.UTC;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(UTC_ZONE);
    private static final long ACCESS_TOKEN_VALIDITY_HOURS = 8L;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 60L;
    private static final long BLACKLIST_EXTRA_HOURS = 1L;
    private static final int TOKEN_ID_LENGTH = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final Key key;

    public TokenProvider(RedisTemplate<String, String> redisTemplate,
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
        Claims claims;
        try {
            claims = getClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }

        String githubId = claims.get("githubId", String.class);
        String blacklistKey = "blacklist:" + githubId + ":" + generateTokenId(token);

        return Boolean.FALSE.equals(redisTemplate.hasKey(blacklistKey)) &&
               claims.getExpiration().after(Date.from(ZonedDateTime.now(UTC_ZONE).toInstant()));
    }

    public void invalidateToken(String token) {
        Claims claims = getClaims(token);
        String githubId = claims.get("githubId", String.class);
        ZonedDateTime tokenExpiresAt = ZonedDateTime.ofInstant(claims.getExpiration().toInstant(), UTC_ZONE);
        ZonedDateTime now = ZonedDateTime.now(UTC_ZONE);
        ZonedDateTime blacklistExpiresAt = tokenExpiresAt.plusHours(BLACKLIST_EXTRA_HOURS);

        String blacklistKey = "blacklist:" + githubId + ":" + generateTokenId(token);
        Map<String, String> blacklistEntry = new HashMap<>();
        blacklistEntry.put("tokenExpiresAt", formatDateTime(tokenExpiresAt));
        blacklistEntry.put("invalidatedAt", formatDateTime(now));
        blacklistEntry.put("blacklistExpiresAt", formatDateTime(blacklistExpiresAt));
        blacklistEntry.put("token", token);

        redisTemplate.opsForHash().putAll(blacklistKey, blacklistEntry);
        redisTemplate.expire(blacklistKey, ChronoUnit.SECONDS.between(now, blacklistExpiresAt), TimeUnit.SECONDS);
    }

    private Date getExpireDateAccessToken() {
        return Date.from(ZonedDateTime.now(UTC_ZONE).plusHours(ACCESS_TOKEN_VALIDITY_HOURS).toInstant());
    }

    private Date getExpireDateRefreshToken() {
        return Date.from(ZonedDateTime.now(UTC_ZONE).plusDays(REFRESH_TOKEN_VALIDITY_DAYS).toInstant());
    }

    private String generateTokenId(String token) {
        return token.substring(Math.max(0, token.length() - TOKEN_ID_LENGTH));
    }

    private String formatDateTime(ZonedDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }
}