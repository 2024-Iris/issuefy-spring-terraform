package site.iris.issuefy.repository;

import org.springframework.stereotype.Repository;

import site.iris.issuefy.entity.User;

@Repository
public class UserRepository {

	public User findByGithubId(String githubId) {
		return new User();
	}
}
