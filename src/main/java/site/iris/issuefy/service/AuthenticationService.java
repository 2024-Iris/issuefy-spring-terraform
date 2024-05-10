package site.iris.issuefy.service;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.vo.OauthDto;
import site.iris.issuefy.vo.UserDto;

@Slf4j
@Service
public class AuthenticationService {

	private static final int KEY_INDEX = 0;
	private static final int VALUE_INDEX = 1;
	private static final int REQUIRE_SIZE = 2;
	private final GithubAccessTokenService githubAccessTokenService;
	private final WebClient webClient;

	// 2개의 WebClient Bean중에서 apiWebClient Bean을 사용하기 위해 생성자를 만들었습니다.
	@Autowired
	public AuthenticationService(GithubAccessTokenService githubAccessTokenService,
		@Qualifier("apiWebClient") WebClient webClient) {
		this.githubAccessTokenService = githubAccessTokenService;
		this.webClient = webClient;
	}

	public UserDto githubLogin(String code) {
		String accessToken = githubAccessTokenService.getToken(code);
		log.info("accessToken : {}", accessToken);

		OauthDto oauthDto = parseOauthDto(accessToken);
		log.info(oauthDto.toString());

		return getUserInfo(oauthDto);
	}

	private OauthDto parseOauthDto(String accessToken) {
		ConcurrentMap<String, String> responseMap = new ConcurrentHashMap<>();
		String[] pair = accessToken.split("&");

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
		return webClient.get()
			.uri("/user")
			.headers(headers -> {
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.setBearerAuth(oauthDto.getAccess_token());
			})
			.retrieve()
			.bodyToMono(UserDto.class)
			.block();
	}
}