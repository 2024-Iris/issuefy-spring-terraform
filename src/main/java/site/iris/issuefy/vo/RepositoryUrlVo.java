package site.iris.issuefy.vo;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RepositoryUrlVo(String repositoryUrl) {

	private static final String INVALID_URL_MESSAGE = "Invalid repository URL";

	public RepositoryUrlVo(String repositoryUrl) {
		String decodedUrl = URLDecoder.decode(repositoryUrl, StandardCharsets.UTF_8);
		Pattern pattern = Pattern.compile("^https://github\\.com/[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+");
		Matcher matcher = pattern.matcher(decodedUrl);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(INVALID_URL_MESSAGE);
		}
		this.repositoryUrl = decodedUrl;
	}
}
