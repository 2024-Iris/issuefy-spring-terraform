package site.iris.issuefy.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.entity.Subscription;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.NotificationDto;
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

    public void handleRedisMessage(String repositoryIdStr) {
        try {
            Long repositoryId = Long.parseLong(repositoryIdStr.trim());
            processRepositoryUpdate(repositoryId);
        } catch (NumberFormatException e) {
            log.error("Invalid repository ID received: {}", repositoryIdStr, e);
        }
    }

    @Transactional
    public void processRepositoryUpdate(Long repositoryId) {
        List<Subscription> subscriptions = subscriptionRepository.findByRepositoryId(repositoryId);
        for (Subscription subscription : subscriptions) {
            String githubId = subscription.getUser().getGithubId();
            sendNotificationToUser(githubId);
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

    @Transactional(readOnly = true)
    private NotificationDto getNotification(String githubId) {
        User user = userRepository.findByGithubId(githubId)
            .orElseThrow(() -> new RuntimeException("User not found with githubId: " + githubId));

        int unreadCount = userNotificationRepository.countByUserIdAndIsReadFalse(user.getId());
        unreadCount++;
        List<String> latestMessages = userNotificationRepository
            .findTop5ByUserIdAndIsReadFalseOrderByNotificationPushTimeDesc(user.getId())
            .stream()
            .map(un -> un.getNotification().getMessage())
            .collect(Collectors.toList());

        return new NotificationDto(unreadCount, latestMessages);
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