package site.iris.issuefy.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.util.ContainerIdUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

	private static final String CONTAINER_ID = ContainerIdUtil.getContainerId();
	private final RedisTemplate<String, String> redisTemplate;
	private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter connect(String githubId) {
		String currentContainer = redisTemplate.opsForValue().get("emitter:" + githubId);

		if (currentContainer != null && !currentContainer.equals(CONTAINER_ID)) {
			redisTemplate.convertAndSend("disconnect", githubId);
		}

		if (isConnected(githubId)) {
			removeEmitter(githubId);
		}

		SseEmitter emitter = new SseEmitter(60000L);
		addEmitter(githubId, emitter);
		initConnect(emitter);

		emitter.onTimeout(() -> removeEmitter(githubId));
		emitter.onCompletion(() -> removeEmitter(githubId));

		return emitter;
	}

	public void handleDisconnect(String githubId) {
		log.info("Received disconnect message for user: {}", githubId);
		if (isConnected(githubId)) {
			removeEmitter(githubId);
		}
	}

	public void addEmitter(String githubId, SseEmitter emitter) {
		redisTemplate.opsForValue().set("emitter:" + githubId, CONTAINER_ID);
		emitters.put(githubId, emitter);
	}

	public void removeEmitter(String githubId) {
		redisTemplate.delete("emitter:" + githubId);
		emitters.remove(githubId);
	}

	public void sendEventToUser(String githubId, String eventName, Object data) {
		SseEmitter emitter = emitters.get(githubId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.id(String.valueOf(System.currentTimeMillis()))
					.name(eventName)
					.data(data));
			} catch (IOException e) {
				log.error("Failed to send event to user: {}", githubId, e);
				removeEmitter(githubId);
			}
		}
	}

	public boolean isConnected(String githubId) {
		return emitters.containsKey(githubId);
	}

	private void initConnect(SseEmitter sseEmitter) {
		try {
			sseEmitter.send(SseEmitter.event()
				.name("initial")
				.data("{\"message\": \"Connection established\"}"));
		} catch (IOException e) {
			log.error("fail init connection");
		}
	}
}