package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import site.iris.issuefy.entity.User;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.vo.UserDto;

class UserServiceTest {

	@InjectMocks
	UserService userService;

	@Mock
	UserRepository userRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("신규회원이 로그인을 시도할 경우 DB에 저장한다.")
	@Test
	void registerUserIfNotExist() {
		// given
		UserDto loginUserDto = new UserDto("dokkisan", "https://avatars.githubusercontent.com/u/117690393?v=4");

		// when
		when(userRepository.findByGithubId(loginUserDto.getGithubId())).thenReturn(Optional.empty());
		userService.registerUserIfNotExist(loginUserDto);

		// then
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository, times(1)).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();
		assertEquals(loginUserDto.getGithubId(), savedUser.getGithubId());
	}
}