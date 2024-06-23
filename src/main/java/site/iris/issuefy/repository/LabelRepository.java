package site.iris.issuefy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Label;

public interface LabelRepository extends CrudRepository<Label, Long> {
	Optional<Label> findByNameAndColor(String name, String color);
	Optional<List<Label>> findByIssue_id(Long issueId);
}
