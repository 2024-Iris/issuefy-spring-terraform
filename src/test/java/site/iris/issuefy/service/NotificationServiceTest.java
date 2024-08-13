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

import site.iris.issuefy.entity.*;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.exception.network.SseException;
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
    private ConcurrentHashMap<String, SseEmitter> sseEmitters;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    @DisplayName("최초 SSE 연결 생성 시 예외가 발생하면 SseException을 던진다.")
    void sendInitialNotification_throwsSseException() throws IOException {
        String githubId = "testUser";
        SseEmitter emitter = mock(SseEmitter.class);
        User user = new User(1L, "testId", "test@email.com", false);

        when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
        when(userNotificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5);
        doThrow(new IOException()).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        assertThrows(SseException.class, () -> notificationService.sendInitialNotification(githubId, emitter));
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

        verify(sseEmitters).put(githubId, emitter);
    }

    @Test
    @DisplayName("ConcurrentHashMap에서 SSE 커넥션 정보를 가져온다.")
    void getEmitter() {
        String githubId = "testUser";
        SseEmitter emitter = new SseEmitter();

        when(sseEmitters.get(githubId)).thenReturn(emitter);

        SseEmitter result = notificationService.getEmitter(githubId);

        assertEquals(emitter, result);
        verify(sseEmitters).get(githubId);
    }

    @Test
    @DisplayName("ConcurrentHashMap에서 SSE 커넥션 정보를 삭제한다.")
    void removeEmitter() {
        String githubId = "testUser";

        notificationService.removeEmitter(githubId);

        verify(sseEmitters).remove(githubId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자에 대해 UserNotFoundException을 던진다.")
    void getNotification_throwsUserNotFoundException() {
        String githubId = "nonExistentUser";

        when(userRepository.findByGithubId(githubId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> notificationService.getNotification(githubId));
    }

    @Test
    @DisplayName("알림을 유저에게 보내는 데 실패하면 SseException을 던진다.")
    void sendNotificationToUser_throwsSseException() throws IOException {
        String githubId = "testUser";
        User user = new User(1L, "testId", "test@email.com", false);
        SseEmitter emitter = mock(SseEmitter.class);

        when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(user));
        when(userNotificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5);
        when(sseEmitters.get(githubId)).thenReturn(emitter);
        doThrow(new IOException()).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        assertThrows(SseException.class, () -> notificationService.sendNotificationToUser(githubId));
    }
}