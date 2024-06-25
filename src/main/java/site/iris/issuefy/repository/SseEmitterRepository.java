package site.iris.issuefy.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SseEmitterRepository {
	private final RedisTemplate<String, String> redisTemplate;
	private static final String KEY_PREFIX = "sse:";

	public void save(Long userId, SseEmitter emitter) {
		String key = KEY_PREFIX + userId;
		redisTemplate.opsForValue().set(key, "active");
		emitter.onCompletion(() -> redisTemplate.delete(key));
		emitter.onTimeout(() -> redisTemplate.delete(key));
	}

	public boolean exists(Long userId) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + userId));
	}

	public void remove(Long userId) {
		redisTemplate.delete(KEY_PREFIX + userId);
	}
}
