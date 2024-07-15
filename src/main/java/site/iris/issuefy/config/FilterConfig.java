package site.iris.issuefy.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.component.LambdaKey;
import site.iris.issuefy.filter.JwtAuthenticationFilter;
import site.iris.issuefy.service.TokenProvider;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

	private final TokenProvider tokenProvider;
	private final LambdaKey lambdaKey;

	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter() {
		FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new JwtAuthenticationFilter(tokenProvider, lambdaKey));
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}
}
