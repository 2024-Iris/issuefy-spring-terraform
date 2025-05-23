package site.iris.issuefy.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import site.iris.issuefy.entity.Org;

public interface OrgRepository extends CrudRepository<Org, Long> {
	Optional<Org> findByName(String name);
}
