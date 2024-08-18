package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import site.iris.issuefy.util.ContainerIdUtil;

class SseServiceTest {

	@InjectMocks
	private SseService sseService;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		sseService = new SseService(redisTemplate);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	@DisplayName("SSE 연결을 생성한다")
	void connect() {
		String githubId = "testUser";
		when(valueOperations.get("emitter:" + githubId)).thenReturn(null);

		SseEmitter result = sseService.connect(githubId);

		assertNotNull(result);
		verify(valueOperations).set("emitter:" + githubId, ContainerIdUtil.getContainerId());
	}

	@Test
	@DisplayName("이미 연결된 사용자의 연결을 갱신한다")
	void connect_existingConnection() {
		String githubId = "testUser";
		when(valueOperations.get("emitter:" + githubId)).thenReturn("otherContainer");

		SseEmitter result = sseService.connect(githubId);

		assertNotNull(result);
		verify(redisTemplate).convertAndSend("disconnect", githubId);
		verify(valueOperations).set("emitter:" + githubId, ContainerIdUtil.getContainerId());
	}
}