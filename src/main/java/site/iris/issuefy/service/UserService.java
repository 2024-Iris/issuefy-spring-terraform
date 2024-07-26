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

	public UserDto getUserInfo(String githubId) {
		User user = userRepository.findByGithubId(githubId).orElseThrow();
		return UserDto.of(user.getGithubId(), user.getEmail(), user.isAlertStatus());
	}

	public void updateEmail(String githubId, String email) {
		User user = userRepository.findByGithubId(githubId).orElseThrow();
		user.updateEmail(email);
		userRepository.save(user);
	}

	public void updateAlert(String githubId, boolean alertStatus) {
		User user = userRepository.findByGithubId(githubId).orElseThrow();
		user.updateAlertStatus(alertStatus);
		userRepository.save(user);
	}

	public void withdraw(String githubId) {
		userRepository.deleteByGithubId(githubId);
	}

	private UserVerifyDto verifyUser(UserDto userDto) {
		boolean exists = userRepository.existsByGithubId(userDto.getGithubId());
		return UserVerifyDto.from(exists);
	}
}
