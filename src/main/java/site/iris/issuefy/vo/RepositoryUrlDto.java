package site.iris.issuefy.vo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryUrlDto {

	private static final String INVALID_URL_MESSAGE = "Invalid repository URL";
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
		throw new IllegalArgumentException(INVALID_URL_MESSAGE);
	}

	@Override
	public String toString() {
		return "RepositoryUrlDto{" +
			"repositoryUrl='" + repositoryUrl + '\'' +
			", orgName='" + orgName + '\'' +
			", repoName='" + repositoryName + '\'' +
			'}';
	}
}
