package site.iris.issuefy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organization")
@Getter
@NoArgsConstructor
public class Org {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column
	private long ghOrgId;

	public Org(String name, long ghOrgId) {
		this.name = name;
		this.ghOrgId = ghOrgId;
	}

	@Override
	public String toString() {
		return "Org{" +
			"id=" + id +
			", name='" + name + '\'' +
			'}';
	}
}
