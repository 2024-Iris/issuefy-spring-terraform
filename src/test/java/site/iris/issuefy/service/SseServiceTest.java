package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

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

	@Mock
	private SseEmitter mockEmitter;

	private final String GITHUB_ID = "testUser";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	@DisplayName("SSE 연결을 생성한다.")
	void connect() {
		when(valueOperations.get("emitter:" + GITHUB_ID)).thenReturn(null);

		SseEmitter result = sseService.connect(GITHUB_ID);

		assertNotNull(result);
		verify(valueOperations).set("emitter:" + GITHUB_ID, ContainerIdUtil.getContainerId());
	}

	@Test
	@DisplayName("이미 연결된 사용자의 연결을 갱신한다.")
	void connect_existingConnection() {
		when(valueOperations.get("emitter:" + GITHUB_ID)).thenReturn("otherContainer");

		SseEmitter result = sseService.connect(GITHUB_ID);

		assertNotNull(result);
		verify(redisTemplate).convertAndSend("disconnect", GITHUB_ID);
		verify(valueOperations).set("emitter:" + GITHUB_ID, ContainerIdUtil.getContainerId());
	}

	@Test
	@DisplayName("연결 해제를 정상적으로 처리한다.")
	void handleDisconnect() {
		sseService.addEmitter(GITHUB_ID, mockEmitter);
		sseService.handleDisconnect(GITHUB_ID);

		assertFalse(sseService.isConnected(GITHUB_ID));
		verify(redisTemplate).delete("emitter:" + GITHUB_ID);
	}

	@Test
	@DisplayName("이벤트가 발생하면 사용자가에 알림을 전송한다.")
	void sendEventToUser() throws IOException {
		sseService.addEmitter(GITHUB_ID, mockEmitter);

		sseService.sendEventToUser(GITHUB_ID, "testEvent", "testData");

		verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
	}

	@Test
	@DisplayName("사용자에게 이벤트 전송 실패 시 Emitter를 제거한다.")
	void sendEventToUser_FailureRemovesEmitter() throws IOException {
		sseService.addEmitter(GITHUB_ID, mockEmitter);
		doThrow(new IOException()).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

		sseService.sendEventToUser(GITHUB_ID, "testEvent", "testData");

		assertFalse(sseService.isConnected(GITHUB_ID));
		verify(redisTemplate).delete("emitter:" + GITHUB_ID);
	}

	@Test
	@DisplayName("연결 상태를 확인한다.")
	void isConnected() {
		sseService.addEmitter(GITHUB_ID, mockEmitter);
		assertTrue(sseService.isConnected(GITHUB_ID));

		sseService.removeEmitter(GITHUB_ID);
		assertFalse(sseService.isConnected(GITHUB_ID));
	}

	@Test
	@DisplayName("초기 연결 메시지를 전송한다.")
	void initConnect() {
		sseService.connect(GITHUB_ID);

		verify(valueOperations).set(eq("emitter:" + GITHUB_ID), anyString());
	}
}