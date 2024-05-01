package site.iris.issuefy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OauthService {

	@Value("${github.client-secret}")
	private String clientSecret;

	@Value("${github.client-id}")
	private String clientId;

	private final WebClient webClient;

	public OauthService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://github.com").build();
	}

	public void githubLogin(String code) {
		log.info(sendRequest(code));

	}

	public String sendRequest(String code) {
		return webClient.post()
			.uri("/login/oauth/access_token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData("client_id", clientId)
				.with("client_secret", clientSecret)
				.with("code", code))
			.retrieve()
			.bodyToMono(String.class)
			.block();
	}
}
