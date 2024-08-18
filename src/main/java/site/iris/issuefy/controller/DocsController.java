package site.iris.issuefy.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DocsController {
	private final ResourceLoader resourceLoader;

	@GetMapping("/api/docs")
	public ResponseEntity<Resource> getDocs() {
		Resource resource = resourceLoader.getResource("classpath:/static/docs/api-guide.html");
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_TYPE, "text/html")
			.body(resource);
	}
}
