package site.iris.issuefy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.iris.issuefy.domain.User;
import site.iris.issuefy.dto.UserRequest;
import site.iris.issuefy.service.UserService;

@WebMvcTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockBean
	private UserService userService;

	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(documentationConfiguration(restDocumentation))
			.build();
	}

	// @Test
	// void join() throws Exception {
	// 	User user = new User(1L);
	//
	// 	// mockMvc.perform((RequestBuilder)post("/users")
	// 	// 		.accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)))
	// 	// 	.andExpect(status().isCreated());
	// 	this.mockMvc.perform(post("/users").accept(MediaType.APPLICATION_JSON))
	// 		.andExpect(status().isCreated())
	// 		.andDo(document("index", links(
	// 			linkWithRel("alpha").description("Link to the alpha resource"),
	// 			linkWithRel("bravo").description("Link to the bravo resource"))));
	//
	//
	// }

	@Test
	void join() throws Exception {
		UserRequest userRequest = new UserRequest();
		User user = new User(1L); // 예시로 추가한 클래스 필드 설정 필요
		userRequest.setEmail("user@example.com"); // 필요한 필드 추가

		String userJson = objectMapper.writeValueAsString(userRequest); // ObjectMapper 필요

		// UserService 목킹 동작 설정
		when(userService.create(any(UserRequest.class))).thenReturn(user);

		this.mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)// 요청 본문의 타입 지정
				.content(userJson) // 실제 요청 본문 추가
				.accept(MediaType.APPLICATION_JSON)) // JSON 응답 수락
			.andExpect(status().isCreated()) // 상태 코드 201 Created 기대
			.andDo(document("join",
				requestFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("The user's ID"),
					fieldWithPath("email").type(JsonFieldType.STRING).description("The user's email address")
				),
				responseFields( // 응답 필드 문서화
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("The user's ID"),
					fieldWithPath("email").type(JsonFieldType.STRING).description("The user's email address")
				)));
	}

	private RestDocumentationResultHandler createSnippets() {
		return document("build/generated-snippets",
			requestFields(
				fieldWithPath("id").type(JsonFieldType.STRING).description("The user's id")
			),
			requestFields(
				fieldWithPath("email").type(JsonFieldType.STRING).description("The user's email address")
			)
		);
	}
}
