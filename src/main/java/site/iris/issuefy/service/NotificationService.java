package site.iris.issuefy.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Notification;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.entity.UserNotification;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.exception.resource.UserNotFoundException;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.model.dto.NotificationReadDto;
import site.iris.issuefy.model.dto.UnreadNotificationDto;
import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.repository.NotificationRepository;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserNotificationRepository;
import site.iris.issuefy.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
	private final UserNotificationRepository userNotificationRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		subscribeToRedis();
	}

	private void subscribeToRedis() {
		Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().subscribe(
			(message, pattern) -> handleRedisMessage(String.valueOf(message)),
			"notifications".getBytes()
		);
		log.info("Subscribed to Redis channel: notifications");
	}

	public void handleUpdateRepositoryDto(UpdateRepositoryDto updateRepositoryDto) {
		try {
			String message = new ObjectMapper().writeValueAsString(updateRepositoryDto);
			redisTemplate.convertAndSend("notifications", message);
			log.info("Published notification to Redis for repositories: {}",
				updateRepositoryDto.getUpdatedRepositoryIds());
		} catch (JsonProcessingException e) {
			log.error("Error publishing notification to Redis", e);
		}
	}

	public void handleRedisMessage(String message) {
		try {
			UpdateRepositoryDto updateRepositoryDto = new ObjectMapper().readValue(message, UpdateRepositoryDto.class);
			processUpdateRepositoryDto(updateRepositoryDto);
		} catch (JsonProcessingException e) {
			log.error("Error parsing Redis message", e);
		}
	}

	private void processUpdateRepositoryDto(UpdateRepositoryDto updateRepositoryDto) {
		for (String repositoryId : updateRepositoryDto.getUpdatedRepositoryIds()) {
			Long repoId = Long.parseLong(repositoryId);
			processNotificationForRepository(repoId);
		}
	}

	private void processNotificationForRepository(Long repositoryId) {
		List<Subscription> subscriptions = subscriptionRepository.findByRepositoryId(repositoryId).orElseThrow();
		Notification notification = new Notification(subscriptions.get(0).getRepository(),
			subscriptions.get(0).getRepository().getName(),
			LocalDateTime.now());
		notificationRepository.save(notification);

		for (Subscription subscription : subscriptions) {
			User user = subscription.getUser();
			UserNotification userNotification = new UserNotification(user, notification);
			userNotificationRepository.save(userNotification);
			sendNotificationToUser(user.getGithubId());
		}
	}

	public void sendNotificationToUser(String githubId) {
		SseEmitter emitter = emitters.get(githubId);
		if (emitter != null) {
			try {
				UnreadNotificationDto dto = getNotification(githubId);
				emitter.send(SseEmitter.event()
					.id(String.valueOf(System.currentTimeMillis()))
					.name("notification")
					.data(dto));
			} catch (IOException e) {
				log.error("Failed to send notification to user: {}", githubId, e);
				removeEmitter(githubId);
			}
		}
	}

	public void addEmitter(String githubId, SseEmitter emitter) {
		emitters.put(githubId, emitter);
	}

	public void removeEmitter(String githubId) {
		emitters.remove(githubId);
	}

	public void sendInitialNotification(String githubId, SseEmitter emitter) {
		try {
			UnreadNotificationDto dto = getNotification(githubId);
			emitter.send(SseEmitter.event().id("0").name("initial").data(dto));
		} catch (IOException e) {
			log.error("Failed to send initial notification to user: {}", githubId, e);
			removeEmitter(githubId);
		}
	}

	public UnreadNotificationDto getNotification(String githubId) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserNotFoundException(ErrorCode.NOT_EXIST_USER.getMessage(),
				ErrorCode.NOT_EXIST_USER.getStatus(), githubId));

		int unreadCount = userNotificationRepository.countByUserIdAndIsReadFalse(user.getId());
		return new UnreadNotificationDto(unreadCount);
	}

	public List<NotificationDto> findNotifications(String githubId) {
		List<UserNotification> userNotificationList = userNotificationRepository.findUserNotificationsByUserGithubId(
			githubId);
		return userNotificationList.stream()
			.map(this::convertToNotificationDto)
			.collect(Collectors.toList());
	}

	@Transactional
	public void updateUserNotificationsAsRead(NotificationReadDto notificationReadDto) {
		userNotificationRepository.markAsRead(notificationReadDto.getUserNotificationIds());
	}

	private NotificationDto convertToNotificationDto(UserNotification userNotification) {
		Notification notification = userNotification.getNotification();
		String orgName = notification.getRepository().getOrg().getName();
		return NotificationDto.of(
			userNotification.getId(),
			orgName,
			notification.getRepositoryName(),
			notification.getPushTime(),
			userNotification.getIsRead()
		);
	}
}