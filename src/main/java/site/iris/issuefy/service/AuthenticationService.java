package site.iris.issuefy.service;

import static site.iris.issuefy.model.dto.OauthDto.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.github.GithubApiException;
import site.iris.issuefy.exception.security.AuthenticationException;
import site.iris.issuefy.model.dto.OauthDto;
import site.iris.issuefy.model.dto.UserDto;

@Slf4j
@Service
public class AuthenticationService {

	private static final int KEY_INDEX = 0;
	private static final int VALUE_INDEX = 1;
	private static final int REQUIRE_SIZE = 2;
	private final WebClient webClient;
	private final GithubAccessTokenService githubAccessTokenService;
	private final UserService userService;
	private final GithubTokenService githubTokenService;

	@Autowired
	public AuthenticationService(GithubAccessTokenService githubAccessTokenService,
		@Qualifier("apiWebClient") WebClient webClient,
		UserService userService, GithubTokenService githubTokenService) {
		this.githubAccessTokenService = githubAccessTokenService;
		this.webClient = webClient;
		this.userService = userService;
		this.githubTokenService = githubTokenService;
	}

	public UserDto githubLogin(String code) {
		String accessToken = githubAccessTokenService.githubGetToken(code);
		log.info("Successfully retrieve GitHub access token");
		OauthDto oauthDto = parseOauthDto(accessToken);
		UserDto loginUserDto = githubGetUserInfo(oauthDto);
		githubTokenService.storeAccessToken(loginUserDto.getGithubId(), oauthDto.getAccessToken());
		userService.registerUserIfNotExist(loginUserDto);
		return loginUserDto;
	}

	private OauthDto parseOauthDto(String accessToken) {
		String[] pair = accessToken.split("&");
		ConcurrentMap<String, String> responseMap = Arrays.stream(pair)
			.map(pairStr -> pairStr.split("="))
			.collect(Collectors.toConcurrentMap(keyValue -> keyValue[KEY_INDEX],
				keyValue -> keyValue.length == REQUIRE_SIZE ? keyValue[VALUE_INDEX] : "",
				(key1, key2) -> key1
			));

		validateKey(responseMap);

		return OauthDto.fromMap(responseMap);
	}

	private void validateKey(ConcurrentMap<String, String> responseMap) {
		if (!responseMap.containsKey(KEY_ACCESS_TOKEN) || !responseMap.containsKey(KEY_TOKEN_TYPE)) {
			throw new AuthenticationException(ErrorCode.REQUIRED_KEYS_MISSING.getMessage(),
				ErrorCode.REQUIRED_KEYS_MISSING.getStatus());
		}
	}

	private UserDto githubGetUserInfo(OauthDto oauthDto) {
		try {
			return webClient.get()
				.uri("/user")
				.headers(headers -> {
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
					headers.setBearerAuth(oauthDto.getAccessToken());
				})
				.retrieve()
				.bodyToMono(UserDto.class)
				.block();
		} catch (WebClientResponseException e) {
			throw new GithubApiException(e.getStatusCode(), e.getResponseBodyAsString());
		}
	}
}
