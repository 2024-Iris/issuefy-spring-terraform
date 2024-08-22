package site.iris.issuefy.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.UnreadNotificationDto;
import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.service.NotificationService;
import site.iris.issuefy.service.SseService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {
	private final SseService sseService;
	private final NotificationService notificationService;

	@GetMapping(value = "/api/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> connect(@RequestAttribute String githubId) {
		SseEmitter emitter = sseService.connect(githubId);
		UnreadNotificationDto unreadNotificationDto = notificationService.getNotification(githubId);
		sseService.sendEventToUser(githubId, "info", unreadNotificationDto);
		return ResponseEntity.ok(emitter);
	}

	@PostMapping("/api/receive")
	public void receiveToLambda(@RequestBody UpdateRepositoryDto updateRepositoryDto) {
		notificationService.handleUpdateRepositoryDto(updateRepositoryDto);
	}
}
