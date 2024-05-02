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

	public void githubLogin(String code) {
		String response = sendRequest(code);
		log.info("response : {}", response);
		OauthDto oauthDto = parseOauthDto(response);
		log.info(oauthDto.toString());

		// 아직 DB 작업이 완료되지 않아 엑세스 토큰으로 테스트 요청을 만들었습니다.
		WebClient test = WebClient.create("https://api.github.com");
		String responseBody = test.get()
			.uri(uriBuilder -> uriBuilder
				.path("repos/elastic/elasticsearch/issues")
				.queryParam("state", "open")
				.queryParam("sort", "created")
				.queryParam("direction", "desc")
				.queryParam("labels", "good first issue")
				.build("ownerValue", "repoValue")
			)
			.header("accept", "application/vnd.github+json")
			.header("auth", oauthDto.getAccess_token())
			.retrieve()
			.bodyToMono(String.class)
			.block();

		log.info(responseBody);
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
}
