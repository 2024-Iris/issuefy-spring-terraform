package site.iris.issuefy.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class LambdaKey {
	@Value("${jwt.lambdaKey}")
	private String lambdaKey;
}
