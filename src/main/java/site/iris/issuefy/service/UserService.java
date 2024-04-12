package site.iris.issuefy.service;

import org.springframework.stereotype.Service;

import site.iris.issuefy.domain.User;
import site.iris.issuefy.dto.UserRequest;

@Service
public class UserService {

	public User create(UserRequest userRequest) {
		return new User(userRequest.getId());
	}
}
