package site.iris.issuefy.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "label")
public class Label {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "color")
	private String color;

	@OneToMany(mappedBy = "label", cascade = CascadeType.ALL)
	private List<Issue> issue = new ArrayList<>();

	private Label(String name, String color) {
		this.name = name;
		this.color = color;
	}

	public static Label of(String name, String color) {
		return new Label(name, color);
	}
}
