package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import site.iris.issuefy.entity.Notification;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.entity.UserNotification;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.model.dto.NotificationReadDto;
import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.repository.NotificationRepository;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserNotificationRepository;
import site.iris.issuefy.repository.UserRepository;

class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private UserNotificationRepository userNotificationRepository;
	@Mock
	private SubscriptionRepository subscriptionRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private ConcurrentHashMap<String, SseEmitter> sseEmitters;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("람다에서 수신한 깃허브 리포지토리 ID를 저장하고 해당되는 유저에게 알림을 보낸다.")
	void handleRedisMessage() {
		UpdateRepositoryDto dto = new UpdateRepositoryDto(Arrays.asList("1", "2"));

		notificationService.handleRedisMessage(dto);
		verify(subscriptionRepository, times(2)).findByRepositoryId(anyLong());
	}

	@Test
	@DisplayName("최초 SSE 연결을 생성하고 초기화 메시지를 보낸다.")
	void sendInitialNotification() throws IOException {
		String githubId = "testUser";
		SseEmitter emitter = mock(SseEmitter.class);
		User user = new User(1L, "testId", "test@email.com", false);

		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		when(userNotificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5);

		notificationService.sendInitialNotification(githubId, emitter);

		verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
	}

	@Test
	@DisplayName("업데이트가 발생한 리포지토리를 구독중인 유저를 찾는다.")
	void findSubscribeRepositoryUser() {
		User user = new User(1L, "testId", "test@email.com", false);
		Org org = new Org(1L, "test", 1L);
		Repository repository = new Repository(1L, org, "testRepo", 1L, LocalDateTime.now());
		Subscription subscription = new Subscription(1L, user, repository, false);

		when(subscriptionRepository.findByRepositoryId(1L)).thenReturn(List.of(subscription));

		notificationService.findSubscribeRepositoryUser(1L);

		verify(notificationRepository).save(any(Notification.class));
		verify(userNotificationRepository).save(any(UserNotification.class));
	}

	@Test
	@DisplayName("유저에게 생성된 알림 전체를 반환한다.")
	void findNotifications() {
		User user = new User(1L, "testId", "test@email.com", false);
		Org org = new Org(1L, "testOrg", 1L);
		Repository repository = new Repository(1L, org, "testRepo", 1L, LocalDateTime.now());
		Notification notification = new Notification(1L, repository, "testRepo", LocalDateTime.now());
		UserNotification userNotification = new UserNotification(1L, user, notification, false);

		when(userNotificationRepository.findUserNotificationsByUserGithubId("testId"))
			.thenReturn(List.of(userNotification));

		List<NotificationDto> result = notificationService.findNotifications("testId");

		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		assertEquals("testOrg", result.get(0).getOrgName());
		assertEquals("testRepo", result.get(0).getRepositoryName());
		assertFalse(result.get(0).isRead());
		assertEquals(1L, result.get(0).getUserNotificationId());
	}

	@Test
	@DisplayName("유저에게 생성된 알림을 읽음 처리한다.")
	void updateUserNotificationsAsRead() {
		NotificationReadDto dto = new NotificationReadDto();
		dto.setUserNotificationIds(Arrays.asList(1L, 2L, 3L));

		notificationService.updateUserNotificationsAsRead(dto);

		verify(userNotificationRepository).markAsRead(dto.getUserNotificationIds());
	}

	@Test
	@DisplayName("ConcurrentHashMap에 SSE 커넥션 정보를 삽입한다.")
	void addEmitter() {
		String githubId = "testUser";
		SseEmitter emitter = new SseEmitter();

		notificationService.addEmitter(githubId, emitter);

		verify(sseEmitters, times(1)).put(githubId, emitter);
	}

	@Test
	@DisplayName("ConcurrentHashMap에 SSE 커넥션 정보를 가져온다.")
	void getEmitter() {
		String githubId = "testUser";
		SseEmitter emitter = new SseEmitter();

		when(sseEmitters.get(githubId)).thenReturn(emitter);

		SseEmitter result = notificationService.getEmitter(githubId);

		assertEquals(emitter, result);
		verify(sseEmitters, times(1)).get(githubId);
	}

	@Test
	@DisplayName("ConcurrentHashMap에 SSE 커넥션 정보를 삭제한다.")
	void removeEmitter() {
		String githubId = "testUser";

		notificationService.removeEmitter(githubId);

		verify(sseEmitters, times(1)).remove(githubId);
	}
}