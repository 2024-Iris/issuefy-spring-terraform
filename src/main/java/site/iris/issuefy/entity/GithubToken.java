package site.iris.issuefy.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@RedisHash("GithubToken")
@AllArgsConstructor
public class GithubToken {
	@Id
	private String githubId;

	private String accessToken;

	@TimeToLive
	private long expireTime;

	public static GithubToken of(String githubId, String accessToken, long expireTime) {
		return new GithubToken(githubId, accessToken, expireTime);
	}
}
