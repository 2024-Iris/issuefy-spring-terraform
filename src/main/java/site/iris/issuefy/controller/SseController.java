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
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.UpdateRepositoryDto;
import site.iris.issuefy.service.NotificationService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {
	private final NotificationService notificationService;

	@GetMapping(value = "/api/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> connect(@RequestAttribute String githubId) {
		log.info("New connection for user: {}", githubId);
		SseEmitter emitter = new SseEmitter(60000L);
		notificationService.addEmitter(githubId, emitter);
		notificationService.sendInitialNotification(githubId, emitter);

		emitter.onTimeout(() -> {
			log.info("SSE connection timed out for user: {}", githubId);
			notificationService.removeEmitter(githubId);
			try {
				emitter.send(SseEmitter.event().name("error").data("Connection timed out"));
			} catch (IOException e) {
				log.error("Error sending timeout message", e);
			} finally {
				emitter.complete();
			}
		});

		return ResponseEntity.ok(emitter);
	}

	@PostMapping("/api/receive")
	public void receive(@RequestBody UpdateRepositoryDto updateRepositoryDto) {
		log.info("lambda request receive");
		notificationService.handleRedisMessage(updateRepositoryDto);
	}
}
