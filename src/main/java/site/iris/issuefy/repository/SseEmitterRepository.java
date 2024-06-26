package site.iris.issuefy.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SseEmitterRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final String KEY_PREFIX = "sse:";

    public void addEmitter(String githubId, SseEmitter emitter) {
       emitters.put(githubId, emitter);
       String key = KEY_PREFIX + githubId;
       redisTemplate.opsForValue().set(key, "connected");
       redisTemplate.expire(key, 30, TimeUnit.MINUTES);  // 30분 후 만료

       emitter.onCompletion(() -> {
          removeEmitter(githubId);
       });
       emitter.onTimeout(() -> {
          removeEmitter(githubId);
       });
    }

    public void removeEmitter(String githubId) {
       emitters.remove(githubId);
       String key = KEY_PREFIX + githubId;
       redisTemplate.delete(key);
    }

    public SseEmitter getEmitter(String githubId) {
       return emitters.get(githubId);
    }

    public boolean isUserConnected(String githubId) {
       String key = KEY_PREFIX + githubId;
       return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}