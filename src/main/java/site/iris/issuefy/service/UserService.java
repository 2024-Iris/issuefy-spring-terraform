package site.iris.issuefy.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.repository.UserRepository;
import site.iris.issuefy.vo.UserDto;
import site.iris.issuefy.vo.UserVerifyDto;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public void registerUserIfNotExist(UserDto loginUserDto) {
		UserVerifyDto userVerifyDto = verifyUser(loginUserDto);
		if (!userVerifyDto.isValid()) {
			User user = new User(loginUserDto.getGithubId());
			userRepository.save(user);
		}
	}

	private UserVerifyDto verifyUser(UserDto userDto) {
		Optional<User> user = userRepository.findByGithubId(userDto.getGithubId());
		if (user.isEmpty()) {
			return UserVerifyDto.from(false);
		}
		return UserVerifyDto.from(true);
	}
}
