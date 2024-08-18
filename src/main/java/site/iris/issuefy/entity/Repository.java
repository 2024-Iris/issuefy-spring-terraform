package site.iris.issuefy.entity;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "repository")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Repository {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "org_id", nullable = false)
	private Org org;

	@Column(name = "name")
	private String name;

	@Column
	private long ghRepoId;

	@Column(name = "latest_update_at")
	private LocalDateTime latestUpdateAt;

	public Repository(Org org, String name, long ghRepoId) {
		this.org = org;
		this.name = name;
		this.ghRepoId = ghRepoId;
	}
}
