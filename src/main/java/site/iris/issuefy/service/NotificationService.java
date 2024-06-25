package site.iris.issuefy.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.model.dto.NotificationDto;
import site.iris.issuefy.repository.SseEmitterRepository;
import site.iris.issuefy.repository.SubscriptionRepository;
import site.iris.issuefy.repository.UserNotificationRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

	private final UserNotificationRepository userNotificationRepository;
	private final SseEmitterRepository sseEmitterRepository;
	private final SubscriptionRepository subscriptionRepository;

	public void handleRedisMessage(String repositoryIdStr) {
		try {
			Long repositoryId = Long.parseLong(repositoryIdStr.trim());
			processRepositoryUpdate(repositoryId);
		} catch (NumberFormatException e) {
			log.error("Invalid repository ID received: {}", repositoryIdStr, e);
		}
	}

	public void processRepositoryUpdate(Long repositoryId) {
		List<Subscription> subscriptions = subscriptionRepository.findByRepositoryId(repositoryId);
		for (Subscription subscription : subscriptions) {
			Long userId = subscription.getUser().getId();
			sendNotificationToUser(userId);
		}
	}

	public void sendNotificationToUser(Long userId) {
		NotificationDto notificationDto = getNotification(userId);
		if (sseEmitterRepository.exists(userId)) {
			SseEmitter emitter = new SseEmitter();
			try {
				emitter.send(SseEmitter.event()
					.id(String.valueOf(System.currentTimeMillis()))
					.name("notification")
					.data(notificationDto));
			} catch (IOException e) {
				log.error("Failed to send notification to user {}", userId, e);
				sseEmitterRepository.remove(userId);
			}
		}
	}

	public void sendInitialNotification(Long userId, SseEmitter emitter) {
		try {
			emitter.send(SseEmitter.event()
				.id("0")
				.name("initial")
				.data(getNotification(userId)));
		} catch (IOException e) {
			log.error("Failed to send initial notification to user {}", userId, e);
			sseEmitterRepository.remove(userId);
		}
	}

	private NotificationDto getNotification(Long userId) {
		int unreadCount = userNotificationRepository.countByUserIdAndIsReadFalse(userId);
		List<String> latestMessages = userNotificationRepository
			.findTop5ByUserIdAndIsReadFalseOrderByNotificationPushTimeDesc(userId)
			.stream()
			.map(un -> un.getNotification().getMessage())
			.collect(Collectors.toList());
		return new NotificationDto(unreadCount, latestMessages);
	}

	public void addUserConnection(Long userId, SseEmitter emitter) {
		sseEmitterRepository.save(userId, emitter);
		sendInitialNotification(userId, emitter);
	}

	public void removeUserConnection(Long userId) {
		sseEmitterRepository.remove(userId);
	}
}
