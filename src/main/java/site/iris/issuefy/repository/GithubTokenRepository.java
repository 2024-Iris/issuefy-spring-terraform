package site.iris.issuefy.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GithubTokenRepository {
	private static final long EXPIRE_TIME = 60L * 60 * 8;
	private final RedisTemplate<String, String> redisTemplate;

	public void storeAccessToken(String key, String accessToken) {
		redisTemplate.opsForValue().set(key, accessToken, Duration.ofSeconds(EXPIRE_TIME));
	}

	public String findAccessToken(String key) {
		return redisTemplate.opsForValue().get(key);
	}
}
