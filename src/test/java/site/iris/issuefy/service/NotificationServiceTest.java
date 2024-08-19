package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.entity.Notification;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.entity.Repository;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.entity.UserNotification;
import site.iris.issuefy.exception.resource.UserNotFoundException;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.model.dto.NotificationReadDto;
import site.iris.issuefy.model.dto.UnreadNotificationDto;
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
	private RedisTemplate<String, String> redisTemplate;
	@Mock
	private SseService sseService;
	@Mock
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("리포지토리 업데이트를 처리하고 알림을 생성한다.")
	void handleUpdateRepositoryDto() {
		UpdateRepositoryDto dto = new UpdateRepositoryDto(Arrays.asList("1", "2"));
		User user = new User(1L, "githubuser1", "test@email.com", false);
		Repository repository = new Repository(1L, new Org(), "testRepo", 1L, LocalDateTime.now());
		Subscription subscription = new Subscription(user, repository);

		when(subscriptionRepository.findByRepositoryId(anyLong())).thenReturn(Optional.of(List.of(subscription)));
		when(sseService.isConnected(anyString())).thenReturn(false);
		when(userRepository.findByGithubId(anyString())).thenReturn(Optional.of(user));  // 이 줄 추가
		when(userNotificationRepository.countByUserIdAndIsReadFalse(anyLong())).thenReturn(0);  // 이 줄 추가

		notificationService.handleUpdateRepositoryDto(dto);

		verify(notificationRepository, times(2)).save(any(Notification.class));
		verify(userNotificationRepository, times(2)).save(any(UserNotification.class));
		verify(redisTemplate, times(2)).convertAndSend(eq("notifications"), anyString());
	}

	@Test
	@DisplayName("유저의 읽지 않은 알림 개수를 반환한다.")
	void getNotification() {
		String githubId = "testUser";
		User user = new User(1L, "testId", "test@email.com", false);

		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
		when(userNotificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5);

		UnreadNotificationDto result = notificationService.getNotification(githubId);

		assertEquals(5, result.getUnreadCount());
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
	@DisplayName("존재하지 않는 사용자에 대해 UserNotFoundException을 던진다.")
	void getNotification_throwsUserNotFoundException() {
		String githubId = "nonExistentUser";

		when(userRepository.findByGithubId(githubId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> notificationService.getNotification(githubId));
	}

	@Test
	@DisplayName("Redis 메시지를 처리하고 연결된 클라이언트에게 알림을 전송한다.")
	void handleRedisMessage_connectedClient() throws Exception {
		String message = "{\"githubId\":\"testUser\",\"notification\":{\"unreadCount\":5},\"senderId\":\"sender1\"}";
		ObjectMapper realObjectMapper = new ObjectMapper();
		when(objectMapper.readTree(message)).thenReturn(realObjectMapper.readTree(message));
		when(sseService.isConnected("testUser")).thenReturn(true);

		notificationService.handleRedisMessage(message);

		verify(sseService).sendEventToUser(eq("testUser"), eq("notification"), any(UnreadNotificationDto.class));
	}

	@Test
	@DisplayName("Redis 메시지를 처리할 때 연결되지 않은 클라이언트에게는 알림을 전송하지 않는다.")
	void handleRedisMessage_disconnectedClient() throws Exception {
		String message = "{\"githubId\":\"testUser\",\"notification\":{\"unreadCount\":5},\"senderId\":\"sender1\"}";
		ObjectMapper realObjectMapper = new ObjectMapper();
		when(objectMapper.readTree(message)).thenReturn(realObjectMapper.readTree(message));
		when(sseService.isConnected("testUser")).thenReturn(false);

		notificationService.handleRedisMessage(message);

		verify(sseService, never()).sendEventToUser(anyString(), anyString(), any());
	}
}