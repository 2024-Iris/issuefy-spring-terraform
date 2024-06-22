package site.iris.issuefy.component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.TestDto;

@Component
@Slf4j
public class SseEmitters {

	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	public SseEmitter add(SseEmitter emitter) {
		this.emitters.add(emitter);
		log.info("new emitter added: {}", emitter);
		log.info("emitter list size: {}", emitters.size());
		emitter.onCompletion(() -> {
			log.info("onCompletion callback");
			this.emitters.remove(emitter);
		});
		emitter.onTimeout(() -> {
			log.info("onTimeout callback");
			emitter.complete();
		});

		return emitter;
	}

	 public void pushMessage(TestDto message) {
        log.info("Received message: {}", message);
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("push")
                        .data(message));
            } catch (IOException e) {
                log.error("Error sending SSE event", e);
                emitter.completeWithError(e);
            }
        });
    }
}
