package site.iris.issuefy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
}
