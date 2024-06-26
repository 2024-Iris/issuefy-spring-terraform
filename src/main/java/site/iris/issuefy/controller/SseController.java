package site.iris.issuefy.controller;


import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.component.SseEmitters;
import site.iris.issuefy.model.dto.TestDto;
import site.iris.issuefy.service.NotificationService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {
	private final SseEmitters sseEmitters;
    private final NotificationService notificationService;
	private final CopyOnWriteArrayList<SseEmitter> sseEmitterList = new CopyOnWriteArrayList<>();

	@GetMapping(value = "/api/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> connect(@RequestAttribute("githubId") String githubId) {
		log.info("New connection for user: {}", githubId);
		SseEmitter emitter = new SseEmitter(45000L);
		sseEmitterList.add(emitter);
		notificationService.addUserConnection(githubId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE connection completed for user: {}", githubId);
            notificationService.removeUserConnection(githubId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE connection timed out for user: {}", githubId);
            notificationService.removeUserConnection(githubId);
        });

        emitter.onError((ex) -> {
            log.error("SSE connection error for user: {}", githubId, ex);
            notificationService.removeUserConnection(githubId);
        });

        return ResponseEntity.ok(emitter);
	}

	@PostMapping("/api/push")
	public ResponseEntity<Void> push() {
		log.info("request test");
		TestDto testDto = new TestDto(1L, "테스트 메시지");
		
		sseEmitterList.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("push")
                        .data(testDto));
            } catch (IOException e) {
                log.error("Error sending SSE event", e);
                emitter.completeWithError(e);
            }
        });
		return ResponseEntity.ok().build();
	}
}
