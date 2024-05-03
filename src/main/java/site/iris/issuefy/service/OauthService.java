package site.iris.issuefy.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.vo.OauthDto;
import site.iris.issuefy.vo.UserDto;

@Slf4j
@Service
public class OauthService {

	@Value("${github.client-secret}")
	private String clientSecret;

	@Value("${github.client-id}")
	private String clientId;

	private final WebClient webClient;
	private static final int KEY_INDEX = 0;
	private static final int VALUE_INDEX = 1;
	private static final int REQUIRE_SIZE = 2;

	public OauthService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://github.com").build();
	}

	public UserDto githubLogin(String code) {
		String response = getToken(code);
		log.info("response : {}", response);
		OauthDto oauthDto = parseOauthDto(response);
		log.info(oauthDto.toString());
		return getUserInfo(oauthDto);
	}

	public String getToken(String code) {
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

	private OauthDto parseOauthDto(String response) {
		ConcurrentMap<String, String> responseMap = new ConcurrentHashMap<>();
		String[] pair = response.split("&");

		for (String pairStr : pair) {
			String[] keyValue = pairStr.split("=");
			if (keyValue.length == REQUIRE_SIZE) {
				responseMap.put(keyValue[KEY_INDEX], keyValue[VALUE_INDEX]);
			} else {
				// null 값 대신 빈 문자열
				responseMap.put(keyValue[KEY_INDEX], "");
			}
		}

		// 필수 키가 없으면 예외 발생
		if (!responseMap.containsKey("access_token") || !responseMap.containsKey("token_type")) {
			throw new IllegalArgumentException("Response does not contain all required keys");
		}

		return OauthDto.fromMap(responseMap);
	}

	private UserDto getUserInfo(OauthDto oauthDto) {
		WebClient userInfo = WebClient.create("https://api.github.com");
		return userInfo.get()
			.uri(uriBuilder -> uriBuilder
				.path("user")
				.build()
			)
			.header("accept", "application/vnd.github+json")
			.header("Authorization", "Bearer " + oauthDto.getAccess_token())
			.retrieve()
			.bodyToMono(UserDto.class)
			.block();
	}
}
