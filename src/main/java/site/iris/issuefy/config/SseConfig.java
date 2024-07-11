package site.iris.issuefy.config;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Configuration
public class SseConfig {
	@Bean
	public ConcurrentHashMap<String, SseEmitter> SseConnectionMap() {
		return new ConcurrentHashMap<>();
	}
}
