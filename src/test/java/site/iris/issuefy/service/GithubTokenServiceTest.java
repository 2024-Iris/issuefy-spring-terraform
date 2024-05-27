package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import site.iris.issuefy.entity.GithubToken;
import site.iris.issuefy.repository.GithubTokenRedisRepository;

@ExtendWith(MockitoExtension.class)
class GithubTokenServiceTest {
    @Mock
    private GithubTokenRedisRepository githubTokenRedisRepository;

    @InjectMocks
    private GithubTokenService githubTokenService;

    private static final String GITHUB_ID = "testUser";
    private static final String ACCESS_TOKEN = "testToken";

    @BeforeEach
    void setUp() {
        githubTokenService = new GithubTokenService(githubTokenRedisRepository);
    }

    @DisplayName("액세스 토큰을 저장한다")
    @Test
    void storeAccessToken() {
        // when
        githubTokenService.storeAccessToken(GITHUB_ID, ACCESS_TOKEN);

        // then
        verify(githubTokenRedisRepository, times(1)).save(any(GithubToken.class));
    }

    @DisplayName("액세스 토큰을 조회한다")
    @Test
    void findAccessToken() {
        // given
        GithubToken githubToken = GithubToken.of(GITHUB_ID, ACCESS_TOKEN, 60L * 60 * 8);
        when(githubTokenRedisRepository.findById(GITHUB_ID)).thenReturn(Optional.of(githubToken));

        // when
        String accessToken = githubTokenService.findAccessToken(GITHUB_ID);

        // then
        assertEquals(ACCESS_TOKEN, accessToken);
    }

    @DisplayName("액세스 토큰이 없을 경우 'Token not found'를 반환한다")
    @Test
    void findAccessTokenNotFound() {
        // given
        when(githubTokenRedisRepository.findById(GITHUB_ID)).thenReturn(Optional.empty());

        // when
        String accessToken = githubTokenService.findAccessToken(GITHUB_ID);

        // then
        assertEquals("Token not found", accessToken);
    }

    @DisplayName("액세스 토큰을 삭제한다")
    @Test
    void deleteAccessToken() {
        // when
        githubTokenService.deleteAccessToken(GITHUB_ID);

        // then
        verify(githubTokenRedisRepository, times(1)).deleteById(GITHUB_ID);
    }
}