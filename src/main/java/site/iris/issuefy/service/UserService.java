package site.iris.issuefy.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.UserDto;
import site.iris.issuefy.model.dto.UserVerifyDto;
import site.iris.issuefy.repository.UserRepository;

@Service
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;
	private final GithubTokenService githubTokenService;
	private final WebClient webClient;

	@Value("${github.client-secret}")
	private String clientSecret;
	@Value("${github.client-id}")
	private String clientId;

	public UserService(UserRepository userRepository, GithubTokenService githubTokenService,
		@Qualifier("apiWebClient") WebClient webClient) {
		this.userRepository = userRepository;
		this.githubTokenService = githubTokenService;
		this.webClient = webClient;
	}

	public void registerUserIfNotExist(UserDto loginUserDto) {
		UserVerifyDto userVerifyDto = verifyUser(loginUserDto);
		if (!userVerifyDto.isValid()) {
			User user = new User(loginUserDto.getGithubId(), loginUserDto.getEmail());
			userRepository.save(user);
		}
	}

	public UserDto getUserInfo(String githubId) {
		User user = userRepository.findByGithubId(githubId).orElseThrow();
		return UserDto.of(user.getGithubId(), user.getEmail(), user.isAlertStatus());
	}

	public void updateEmail(String githubId, String email) {
		User user = userRepository.findByGithubId(githubId).orElseThrow();
		user.updateEmail(email);
		userRepository.save(user);
	}

	public void updateAlert(String githubId, boolean alertStatus) {
		User user = userRepository.findByGithubId(githubId).orElseThrow();
		user.updateAlertStatus(alertStatus);
		userRepository.save(user);
	}

	@Transactional
	public void withdraw(String githubId) {
		deleteGithubAuth(githubId);
		userRepository.deleteByGithubId(githubId);
	}

	private UserVerifyDto verifyUser(UserDto userDto) {
		boolean exists = userRepository.existsByGithubId(userDto.getGithubId());
		return UserVerifyDto.from(exists);
	}

	private void deleteGithubAuth(String githubId) {
		try {
			String accessToken = githubTokenService.findAccessToken(githubId);
			webClient.method(HttpMethod.DELETE)
				.uri("/applications/{client_id}/grant", clientId)
				.headers(headers -> {
					headers.setBasicAuth(clientId, clientSecret);
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.set("Accept", "application/vnd.github+json");
					headers.set("X-GitHub-Api-Version", "2022-11-28");
				})
				.bodyValue(Map.of("access_token", accessToken))
				.retrieve()
				.toBodilessEntity()
				.block();
		} catch (Exception e) {
			log.info(e.getMessage());
			throw new IllegalArgumentException("Failed to delete github auth");
		}
	}
}
