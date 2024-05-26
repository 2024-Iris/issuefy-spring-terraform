package site.iris.issuefy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import site.iris.issuefy.entity.GithubToken;
import site.iris.issuefy.repository.GithubTokenRedisRepository;

@Service
@RequiredArgsConstructor
public class GithubTokenService {
    private final GithubTokenRedisRepository githubTokenRedisRepository;
    private static final long EXPIRE_TIME = 60L * 60 * 8;

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
