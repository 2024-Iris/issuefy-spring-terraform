package site.iris.issuefy.repository;

import org.springframework.stereotype.Repository;

import site.iris.issuefy.entity.User;

@Repository
public class UserRepository {

	public User findByNickname(String nickname) {
		return new User();
	}
}
