package site.iris.issuefy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Notification;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.entity.UserNotification;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.resource.UserNotFoundException;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.model.dto.NotificationReadDto;
import site.iris.issuefy.model.dto.UnreadNotificationDto;
import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.repository.NotificationRepository;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserNotificationRepository;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.util.ContainerIdUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

	private static final String EVENT_NAME = "notification";
	private final UserNotificationRepository userNotificationRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final SseService sseService;

	public void handleUpdateRepositoryDto(UpdateRepositoryDto updateRepositoryDto) {
		for (String repositoryId : updateRepositoryDto.getUpdatedRepositoryIds()) {
			Long repoId = Long.parseLong(repositoryId);
			processNotificationForRepository(repoId);
		}
	}

	private void processNotificationForRepository(Long repositoryId) {
		List<Subscription> subscriptions = subscriptionRepository.findByRepositoryId(repositoryId).orElseThrow();
		for (Subscription subscription : subscriptions) {
			String githubId = subscription.getUser().getGithubId();
			String repositoryName = subscription.getRepository().getName();

			Notification notification = new Notification(subscription.getRepository(), repositoryName,
				LocalDateTime.now());
			notificationRepository.save(notification);

			UserNotification userNotification = new UserNotification(subscription.getUser(), notification);
			userNotificationRepository.save(userNotification);

			UnreadNotificationDto notificationData = getNotification(githubId);

			if (sseService.isConnected(githubId)) {
				sseService.sendEventToUser(githubId, EVENT_NAME, notificationData);
			} else {
				publishNotificationToRedis(githubId, notificationData);
			}
		}
	}

	private void publishNotificationToRedis(String githubId, UnreadNotificationDto notificationData) {
		try {
			String message = new ObjectMapper().writeValueAsString(
				Map.of(
					"githubId", githubId,
					EVENT_NAME, notificationData,
					"senderId", ContainerIdUtil.getContainerId()
				)
			);

			redisTemplate.convertAndSend("notifications", message);
			log.info("Published notification to Redis for user: {}", githubId);
		} catch (JsonProcessingException e) {
			log.error("Error publishing notification to Redis", e);
		}
	}

	public void handleRedisMessage(String message) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(message);
			String githubId = jsonNode.get("githubId").asText();
			String senderId = jsonNode.get("senderId").asText();

			if (senderId.equals(ContainerIdUtil.getContainerId())) {
				return;
			}

			UnreadNotificationDto notificationData = mapper.treeToValue(jsonNode.get(EVENT_NAME),
				UnreadNotificationDto.class);

			if (sseService.isConnected(githubId)) {
				sseService.sendEventToUser(githubId, EVENT_NAME, notificationData);
			}

		} catch (JsonProcessingException e) {
			log.error("Error parsing Redis message", e);
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