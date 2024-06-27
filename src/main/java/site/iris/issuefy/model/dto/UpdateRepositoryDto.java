package site.iris.issuefy.model.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdateRepositoryDto {
	List<String> repositoriesId;

	@Override
	public String toString() {
		return "UpdateRepositoryDto{" +
			"repositoriesId=" + repositoriesId +
			'}';
	}
}
