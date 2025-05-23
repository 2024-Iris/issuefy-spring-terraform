package site.iris.issuefy.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import site.iris.issuefy.exception.github.GithubApiException;

@Service
public class GithubAccessTokenService {

	private final WebClient webClient;
	@Value("${github.client-secret}")
	private String clientSecret;
	@Value("${github.client-id}")
	private String clientId;

	public GithubAccessTokenService(@Qualifier("accessTokenWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	public String githubGetToken(String code) {
		try {
			return webClient.post()
				.uri("/login/oauth/access_token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("client_id", clientId)
					.with("client_secret", clientSecret)
					.with("code", code))
				.retrieve()
				.bodyToMono(String.class)
				.block();
		} catch (WebClientResponseException e) {
			throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
		}
	}
}
