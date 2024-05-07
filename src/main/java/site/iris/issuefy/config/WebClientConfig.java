package site.iris.issuefy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean(name = "apiWebClient")
	public WebClient apiWebClient() {
		return WebClient.builder()
			.baseUrl("https://api.github.com/")
			.build();
	}

	@Bean(name = "accessTokenWebClient")
	public WebClient accessTokenWebClient() {
		return WebClient.builder()
			.baseUrl("https://oauth.access-token.url/")
			.build();
	}
}


