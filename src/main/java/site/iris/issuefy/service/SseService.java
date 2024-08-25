package site.iris.issuefy.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.network.SseException;
import site.iris.issuefy.eums.SseStatus;
import site.iris.issuefy.util.ContainerIdUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

	private static final String PREFIX_EMITTER = "emitter:";
	private final ObjectMapper objectMapper;
	private final RedisTemplate<String, String> redisTemplate;
	private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter connect(String githubId) {
		String currentContainer = redisTemplate.opsForValue().get(PREFIX_EMITTER + githubId);

		if (currentContainer != null && !currentContainer.equals(ContainerIdUtil.containerId)) {
			redisTemplate.convertAndSend("disconnect", githubId);
		}

		if (isConnected(githubId)) {
			completeConnection(githubId);
		}

		SseEmitter emitter = new SseEmitter(60000L);
		addEmitter(githubId, emitter);
		initConnect(githubId, emitter);

		emitter.onTimeout(() -> completeConnection(githubId));

		emitter.onCompletion(() -> completeConnection(githubId));

		return emitter;
	}

	public void handleDisconnect(String githubId) {
		log.info("Received disconnect message for user: {}", githubId);
		if (isConnected(githubId)) {
			completeConnection(githubId);
		}
	}

	public void addEmitter(String githubId, SseEmitter emitter) {
		redisTemplate.opsForValue().set(PREFIX_EMITTER + githubId, ContainerIdUtil.containerId);
		emitters.put(githubId, emitter);
	}

	public void sendEventToUser(String githubId, String eventName, Object data) {
		SseEmitter emitter = emitters.get(githubId);
		if (emitter != null) {
			try {
				emitter.send(
					SseEmitter.event().id(String.valueOf(System.currentTimeMillis())).name(eventName).data(data));
			} catch (IOException e) {
				completeConnection(githubId);
				throw new SseException(ErrorCode.FAILED_SENDING_MESSAGE.getMessage(),
					ErrorCode.FAILED_SENDING_MESSAGE.getStatus());
			}
		}
	}

	public boolean isConnected(String githubId) {
		return emitters.containsKey(githubId);
	}

	private void initConnect(String githubId, SseEmitter sseEmitter) {
		try {
			SseStatus status = SseStatus.INIT_CONNECTION;
			String jsonData = objectMapper.writeValueAsString(status.getData());
			sseEmitter.send(SseEmitter.event()
				.name(status.getEventName())
				.data(jsonData));
		} catch (IOException e) {
			completeConnection(githubId);
			throw new SseException(ErrorCode.FAILED_INIT_CONNECTION.getMessage(),
				ErrorCode.FAILED_INIT_CONNECTION.getStatus());
		}
	}

	public void completeConnection(String githubId) {
		SseEmitter previousConnection = emitters.remove(githubId);
		previousConnection.complete();
		redisTemplate.delete(PREFIX_EMITTER + githubId);
	}
}