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

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.exception.network.SseException;
import site.iris.issuefy.util.ContainerIdUtil;

class SseServiceTest {

	private final String GITHUB_ID = "testUser";
	@InjectMocks
	private SseService sseService;
	@Mock
	private RedisTemplate<String, String> redisTemplate;
	@Mock
	private ValueOperations<String, String> valueOperations;
	@Mock
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	@DisplayName("SSE 연결을 생성하고 60초 타임아웃을 설정한다.")
	void connect() throws IOException {
		when(valueOperations.get("emitter:" + GITHUB_ID)).thenReturn(null);
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		SseEmitter result = sseService.connect(GITHUB_ID);

		assertNotNull(result);
		assertEquals(60000L, result.getTimeout());
		verify(valueOperations).set("emitter:" + GITHUB_ID, ContainerIdUtil.getContainerId());
	}

	@Test
	@DisplayName("이미 연결된 사용자의 연결을 갱신한다.")
	void connect_existingConnection() throws IOException {
		when(valueOperations.get("emitter:" + GITHUB_ID)).thenReturn("otherContainer");
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");

		SseEmitter result = sseService.connect(GITHUB_ID);

		assertNotNull(result);
		verify(redisTemplate).convertAndSend("disconnect", GITHUB_ID);
		verify(valueOperations).set("emitter:" + GITHUB_ID, ContainerIdUtil.getContainerId());
	}

	@Test
	@DisplayName("연결 해제를 정상적으로 처리한다.")
	void handleDisconnect() {
		SseEmitter mockEmitter = mock(SseEmitter.class);
		sseService.addEmitter(GITHUB_ID, mockEmitter);
		sseService.handleDisconnect(GITHUB_ID);

		assertFalse(sseService.isConnected(GITHUB_ID));
		verify(redisTemplate).delete("emitter:" + GITHUB_ID);
		verify(mockEmitter).complete();
	}

	@Test
	@DisplayName("이벤트가 발생하면 사용자에게 알림을 전송한다.")
	void sendEventToUser() throws IOException {
		SseEmitter mockEmitter = mock(SseEmitter.class);
		sseService.addEmitter(GITHUB_ID, mockEmitter);

		sseService.sendEventToUser(GITHUB_ID, "testEvent", "testData");

		verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
	}

	@Test
	@DisplayName("사용자에게 이벤트 전송 실패 시 Emitter를 제거하고 SseException을 던진다.")
	void sendEventToUser_FailureRemovesEmitterAndThrowsException() throws IOException {
		SseEmitter mockEmitter = mock(SseEmitter.class);
		sseService.addEmitter(GITHUB_ID, mockEmitter);
		doThrow(new IOException()).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

		assertThrows(SseException.class, () -> sseService.sendEventToUser(GITHUB_ID, "testEvent", "testData"));

		assertFalse(sseService.isConnected(GITHUB_ID));
		verify(redisTemplate).delete("emitter:" + GITHUB_ID);
	}

	@Test
	@DisplayName("연결 상태를 확인한다.")
	void isConnected() {
		SseEmitter mockEmitter = mock(SseEmitter.class);
		sseService.addEmitter(GITHUB_ID, mockEmitter);
		assertTrue(sseService.isConnected(GITHUB_ID));

		sseService.completeConnection(GITHUB_ID);
		assertFalse(sseService.isConnected(GITHUB_ID));
	}

	@Test
	@DisplayName("연결 완료 시 Emitter를 제거하고 완료 처리한다.")
	void completeConnection() {
		SseEmitter mockEmitter = mock(SseEmitter.class);
		sseService.addEmitter(GITHUB_ID, mockEmitter);
		sseService.completeConnection(GITHUB_ID);

		assertFalse(sseService.isConnected(GITHUB_ID));
		verify(redisTemplate).delete("emitter:" + GITHUB_ID);
		verify(mockEmitter).complete();
	}
}