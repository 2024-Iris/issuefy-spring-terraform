package site.iris.issuefy.model.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import site.iris.issuefy.exception.InvalidUrlException;

@Getter
@AllArgsConstructor
@ToString
public class RepositoryUrlDto {

	private final String repositoryUrl;
	private final String githubId;
	private final String orgName;
	private final String repositoryName;

	public static RepositoryUrlDto of(String repositoryUrl, String githubId) {
		Pattern pattern = Pattern.compile("^https://github\\.com/([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)");
		Matcher matcher = pattern.matcher(repositoryUrl);
		if (matcher.find()) {
			return new RepositoryUrlDto(repositoryUrl, githubId, matcher.group(1), matcher.group(2));
		}
		throw new InvalidUrlException(InvalidUrlException.INVALID_URL);
	}
}
