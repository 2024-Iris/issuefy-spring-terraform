package site.iris.issuefy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "user")
@ToString
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String githubId;

	@Column
	private String email;

	@Column
	private boolean alertStatus;

	public User(String githubId, String email) {
		this.githubId = githubId;
		this.email = email;
		this.alertStatus = false;
	}

	public void updateEmail(String email) {
		this.email = email;
	}

	public void updateAlertStatus(boolean alertStatus) {
		this.alertStatus = alertStatus;
	}
}
