package site.iris.issuefy.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.User;
import site.iris.issuefy.model.dto.UserDto;
import site.iris.issuefy.model.dto.UserVerifyDto;
import site.iris.issuefy.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public void registerUserIfNotExist(UserDto loginUserDto) {
		UserVerifyDto userVerifyDto = verifyUser(loginUserDto);
		if (!userVerifyDto.isValid()) {
			User user = new User(loginUserDto.getGithubId(), loginUserDto.getEmail());
			userRepository.save(user);
		}
	}

	private UserVerifyDto verifyUser(UserDto userDto) {
		boolean exists = userRepository.existsByGithubId(userDto.getGithubId());
		return UserVerifyDto.from(exists);
	}
}
