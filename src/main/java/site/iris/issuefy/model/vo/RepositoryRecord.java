package site.iris.issuefy.model.vo;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import site.iris.issuefy.exception.InvalidUrlException;

public record RepositoryRecord(String repositoryUrl) {

	public RepositoryRecord(String repositoryUrl) {
		String decodedUrl = URLDecoder.decode(repositoryUrl, StandardCharsets.UTF_8);
		Pattern pattern = Pattern.compile("^https://github\\.com/[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+");
		Matcher matcher = pattern.matcher(decodedUrl);
		if (!matcher.matches()) {
			throw new InvalidUrlException(InvalidUrlException.INVALID_URL);
		}
		this.repositoryUrl = decodedUrl;
	}
}
