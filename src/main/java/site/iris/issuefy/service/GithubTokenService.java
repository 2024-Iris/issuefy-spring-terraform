package site.iris.issuefy.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.GithubToken;
import site.iris.issuefy.repository.GithubTokenRedisRepository;

@Service
@RequiredArgsConstructor
public class GithubTokenService {
	private static final long EXPIRE_TIME = 60L * 60 * 8;
	private final GithubTokenRedisRepository githubTokenRedisRepository;

	public void storeAccessToken(String githubId, String accessToken) {
		GithubToken token = GithubToken.of(githubId, accessToken, EXPIRE_TIME);
		githubTokenRedisRepository.save(token);
	}

	public String findAccessToken(String githubId) {
		return githubTokenRedisRepository.findById(githubId)
			.map(GithubToken::getAccessToken)
			.orElse("Token not found");
	}

	public void deleteAccessToken(String key) {
		githubTokenRedisRepository.deleteById(key);
	}
}
