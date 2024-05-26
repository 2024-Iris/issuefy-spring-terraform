package site.iris.issuefy.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import site.iris.issuefy.entity.GithubToken;

@Repository
public interface GithubTokenRedisRepository extends CrudRepository<GithubToken, String> {

}
