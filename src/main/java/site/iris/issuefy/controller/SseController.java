package site.iris.issuefy.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.component.SseEmitters;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {
	private final SseEmitters sseEmitters;

	@GetMapping(value = "/api/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect() {
        log.info("new connect");
        SseEmitter emitter = new SseEmitter(45000L);
        sseEmitters.add(emitter);
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(emitter);
    }

	@PostMapping("/api/test")
    public ResponseEntity<Void> count() {
        log.info("request test");
        sseEmitters.testMessage();
        return ResponseEntity.ok().build();
    }
}
