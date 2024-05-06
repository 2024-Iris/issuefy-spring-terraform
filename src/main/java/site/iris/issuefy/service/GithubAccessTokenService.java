package site.iris.issuefy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GithubAccessTokenService {

	@Value("${github.client-secret}")
	private String clientSecret;

	@Value("${github.client-id}")
	private String clientId;

	public String getToken(String code) {
		WebClient tokenClient = WebClient.builder().baseUrl("https://github.com").build();
		return tokenClient.post()
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
