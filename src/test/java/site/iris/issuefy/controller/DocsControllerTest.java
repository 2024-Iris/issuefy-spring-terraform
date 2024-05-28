package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocsController.class)
class DocsControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ResourceLoader resourceLoader;

	@BeforeEach
	void setUp() {
		Resource mockResource = mock(Resource.class);
		when(resourceLoader.getResource("classpath:/static/docs/api-guide.html")).thenReturn(mockResource);
	}

	@DisplayName("API 문서를 조회한다")
	@Test
	void getDocs() throws Exception {
		mockMvc.perform(get("/api/docs"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE))
			.andExpect(content().contentType(MediaType.TEXT_HTML));
	}
}