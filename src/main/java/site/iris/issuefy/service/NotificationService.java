package site.iris.issuefy.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Notification;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.entity.UserNotification;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.repository.NotificationRepository;
import site.iris.issuefy.repository.SseEmitterRepository;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserNotificationRepository;
import site.iris.issuefy.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

	private final UserNotificationRepository userNotificationRepository;
	private final SseEmitterRepository sseEmitterRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;

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
			sseEmitterRepository.removeEmitter(githubId);
		}
	}

	private NotificationDto getNotification(String githubId) {
		User user = userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new RuntimeException("User not found with githubId: " + githubId));

		int unreadCount = userNotificationRepository.countByUserIdAndIsReadFalse(user.getId());
		List<String> latestMessages = userNotificationRepository
			.findTop5ByUserIdAndIsReadFalseOrderByNotificationPushTimeDesc(user.getId())
			.stream()
			.map(un -> un.getNotification().getMessage())
			.collect(Collectors.toList());

		return new NotificationDto(unreadCount, latestMessages);
	}

	public void findSubscribeRepositoryUser(Long repositoryId) {
		List<Subscription> subscriptions = subscriptionRepository.findByRepositoryId(repositoryId);
		for (Subscription subscription : subscriptions) {
			String githubId = subscription.getUser().getGithubId();
			sendNotificationToUser(githubId);

			//noti 코드
			String message = subscription.getRepository().getName() + "에서 새로운 이슈가 올라왔어요!";
			Notification notification = new Notification(subscription.getRepository(), message, LocalDateTime.now());
			notificationRepository.save(notification);

			UserNotification userNotification = new UserNotification(subscription.getUser(), notification);
            userNotificationRepository.save(userNotification);
		}
	}

	private void sendNotificationToUser(String githubId) {
		if (!sseEmitterRepository.isUserConnected(githubId)) {
			return;
		}
		try {
			NotificationDto notificationDto = getNotification(githubId);
			SseEmitter emitter = sseEmitterRepository.getEmitter(githubId);
			if (emitter != null) {
				emitter.send(SseEmitter.event()
					.id(String.valueOf(System.currentTimeMillis()))
					.name("notification")
					.data(notificationDto));
			}
		} catch (IOException e) {
			log.error("Failed to send notification to user {}", githubId, e);
			sseEmitterRepository.removeEmitter(githubId);
		} catch (Exception e) {
			log.error("Unexpected error when sending notification to user {}", githubId, e);
		}
	}

	public void addUserConnection(String githubId, SseEmitter emitter) {
		log.debug("Adding user connection for githubId: {}", githubId);
		sseEmitterRepository.addEmitter(githubId, emitter);
		sendInitialNotification(githubId, emitter);
	}

	public void removeUserConnection(String githubId) {
		log.debug("Removing user connection for githubId: {}", githubId);
		sseEmitterRepository.removeEmitter(githubId);
	}
}