package site.iris.issuefy.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GithubTokenRepository {
	private final RedisTemplate<String, String> redisTemplate;
	private static final long EXPIRE_TIME = 60 * 60 * 8;

	public void storeAccessToken(String key, String accessToken) {
		redisTemplate.opsForValue().set(key, accessToken, Duration.ofSeconds(EXPIRE_TIME));
	}

	public String findAccessToken(String key) {
		return redisTemplate.opsForValue().get(key);
	}
}
