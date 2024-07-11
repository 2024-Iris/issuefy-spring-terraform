package site.iris.issuefy.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateRepositoryDto {
	List<String> updatedRepositoryIds;

	@Override
	public String toString() {
		return "UpdateRepositoryDto{" +
			"updatedRepositoryIds=" + updatedRepositoryIds +
			'}';
	}
}
