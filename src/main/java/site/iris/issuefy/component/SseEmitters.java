package site.iris.issuefy.component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.model.dto.TestDto;

@Component
@Slf4j
public class SseEmitters {

	private static final AtomicLong counter = new AtomicLong();

	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	public SseEmitter add(SseEmitter emitter) {
		this.emitters.add(emitter);
		log.info("new emitter added: {}", emitter);
		log.info("emitter list size: {}", emitters.size());
		emitter.onCompletion(() -> {
			log.info("onCompletion callback");
			this.emitters.remove(emitter);    // 만료되면 리스트에서 삭제
		});
		emitter.onTimeout(() -> {
			log.info("onTimeout callback");
			emitter.complete();
		});

		return emitter;
	}

	public void testMessage() {
		long count = counter.incrementAndGet();
		TestDto testDto = new TestDto(1, "테스트 메시지 1");
		emitters.forEach(emitter -> {
			try {
				emitter.send(SseEmitter.event()
					.name("test")
					.data(testDto));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
