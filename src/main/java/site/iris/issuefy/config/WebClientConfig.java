package site.iris.issuefy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebClientConfig {

	@Value("${loki.url}")
	private String lokiUrl;

	@Bean(name = "apiWebClient")
	public WebClient apiWebClient() {
		return WebClient.builder()
			.baseUrl("https://api.github.com/")
			.build();
	}

	@Bean(name = "accessTokenWebClient")
	public WebClient accessTokenWebClient() {
		return WebClient.builder()
			.baseUrl("https://github.com/")
			.build();
	}

	@Bean(name = "lokiWebClient")
	public WebClient lokiWebClient() {
		return WebClient.builder()
			.baseUrl(lokiUrl)
			.build();
	}
}


