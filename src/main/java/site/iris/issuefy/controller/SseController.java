package site.iris.issuefy.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.service.NotificationService;

@RestController
@RequiredArgsConstructor
public class SseController {
	private final NotificationService notificationService;

	@GetMapping(value = "/api/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> connect(@RequestAttribute String githubId) {
		SseEmitter emitter = new SseEmitter(60000L);
		notificationService.addEmitter(githubId, emitter);
		notificationService.sendInitialNotification(githubId, emitter);

		emitter.onTimeout(() -> {
			notificationService.removeEmitter(githubId);
			try {
				emitter.send(SseEmitter.event().name("error").data("Connection timed out"));
			} catch (IOException ignored) {
			} finally {
				emitter.complete();
			}
		});

		return ResponseEntity.ok(emitter);
	}

	@PostMapping("/api/receive")
	public void receive(@RequestBody UpdateRepositoryDto updateRepositoryDto) {
		notificationService.handleRedisMessage(updateRepositoryDto);
	}
}
