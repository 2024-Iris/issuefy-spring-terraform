package site.iris.issuefy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String githubId;

	@Column
	private String email;

	public User(String githubId, String email) {
		this.githubId = githubId;
		this.email = email;
	}

	@Override
	public String toString() {
		return "User{" +
			"id=" + id +
			", githubId='" + githubId + '\'' +
			", email='" + email + '\'' +
			'}';
	}
}
