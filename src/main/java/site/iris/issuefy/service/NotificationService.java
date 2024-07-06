package site.iris.issuefy.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Notification;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.entity.UserNotification;
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
	private final ConcurrentHashMap<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

	public void handleRedisMessage(UpdateRepositoryDto updateRepositoryDto) {
		for (String repository : updateRepositoryDto.getUpdatedRepositoryIds()) {
			Long repositoryId = Long.parseLong(repository);
			findSubscribeRepositoryUser(repositoryId);
		}
	}

	public void sendInitialNotification(String githubId, SseEmitter emitter) {
		try {
			log.debug("Sending initial notification to user {}", githubId);
			emitter.send(SseEmitter.event()
				.id("0")
				.name("initial")
				.data(getNotification(githubId)));
			log.debug("Initial notification sent successfully to user {}", githubId);
		} catch (IOException e) {
			log.error("Failed to send initial notification to user {}", githubId, e);
		}
	}

	private UnreadNotificationDto getNotification(String githubId) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new RuntimeException("User not found with githubId: " + githubId));

		int unreadCount = userNotificationRepository.countByUserIdAndIsReadFalse(user.getId());
		return new UnreadNotificationDto(unreadCount);
	}

	public void findSubscribeRepositoryUser(Long repositoryId) {
		List<Subscription> subscriptions = subscriptionRepository.findByRepositoryId(repositoryId);
		for (Subscription subscription : subscriptions) {
			String githubId = subscription.getUser().getGithubId();
			String repositoryName = subscription.getRepository().getName();
			Notification notification = new Notification(subscription.getRepository(), repositoryName,
				LocalDateTime.now());
			notificationRepository.save(notification);

			UserNotification userNotification = new UserNotification(subscription.getUser(), notification);
			userNotificationRepository.save(userNotification);

			sendNotificationToUser(githubId);
		}
	}

	private void sendNotificationToUser(String githubId) {
		try {
			UnreadNotificationDto unreadNotificationDto = getNotification(githubId);
			SseEmitter emitter = getEmitter(githubId);
			if (emitter != null) {
				emitter.send(SseEmitter.event()
					.id(String.valueOf(System.currentTimeMillis()))
					.name("notification")
					.data(unreadNotificationDto));
			}
		} catch (IOException e) {
			log.error("Failed to send notification to user {}", githubId, e);
		} catch (Exception e) {
			log.error("Unexpected error when sending notification to user {}", githubId, e);
		}
	}

	public List<NotificationDto> findNotifications(String githubId) {
		List<UserNotification> userNotificationList = userNotificationRepository.findUserNotificationsByUserGithubId(
			githubId);
		List<NotificationDto> notificationDtoList = new ArrayList<>();

		for (UserNotification userNotification : userNotificationList) {
			String orgName = userNotification.getNotification().getRepository().getOrg().getName();
			String repositoryName = userNotification.getNotification().getRepositoryName();
			LocalDateTime localDateTime = userNotification.getNotification().getPushTime();
			boolean isRead = userNotification.getIsRead();
			Long userNotificationId = userNotification.getId();

			notificationDtoList.add(
				NotificationDto.of(userNotificationId, orgName, repositoryName, localDateTime, isRead));
		}

		return notificationDtoList;
	}

	public void addEmitter(String githubId, SseEmitter emitter) {
		sseEmitters.put(githubId, emitter);
	}

	public void removeEmitter(String githubId) {
		sseEmitters.remove(githubId);
	}

	public SseEmitter getEmitter(String githubId) {
		return sseEmitters.get(githubId);
	}

	@Transactional
	public void updateUserNotificationsAsRead(NotificationReadDto notificationReadDto) {
		userNotificationRepository.markAsRead(notificationReadDto.getUserNotificationIds());
	}
}