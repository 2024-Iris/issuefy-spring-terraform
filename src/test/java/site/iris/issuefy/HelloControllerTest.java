package site.iris.issuefy;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs
@WebMvcTest(controllers = HelloController.class)
class HelloControllerTest {

	@RegisterExtension
	final RestDocumentationExtension restDocumentation = new RestDocumentationExtension("custom");

	@Autowired
	private MockMvc mockMvc;

	// @BeforeEach
	// void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
	// 	this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
	// 		.apply(documentationConfiguration(restDocumentation))
	// 		.build();
	// }

	@Test
	void hello() throws Exception {
		HelloController helloController = new HelloController();
		assertEquals("hello", helloController.hello());

		mockMvc.perform(get("/hello"))
			.andDo(document("issuefy/hello",
				Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
				Preprocessors.preprocessResponse(Preprocessors.prettyPrint())))
			.andExpect(status().isOk())
			.andDo(print());
	}

}