package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**") // CORS를 적용할 URL 패턴 설정
			.allowedOrigins("http://localhost:3000") // 허용할 Origin 설정
			.allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP 메서드 설정
			.allowedHeaders("*"); // 허용할 HTTP 헤더 설정
	}
}
