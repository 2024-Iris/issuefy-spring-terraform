package site.iris.issuefy.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.model.dto.GithubOrgDto;
import site.iris.issuefy.repository.OrgRepository;

@Service
@RequiredArgsConstructor
public class OrgService {
	private final OrgRepository orgRepository;

	@Transactional
	public Org saveOrg(ResponseEntity<GithubOrgDto> orgInfo) {
		return Optional.ofNullable(orgInfo.getBody())
			.map(body -> orgRepository.findByName(body.getLogin())
				.orElseGet(() -> {
					Org newOrg = new Org(orgInfo.getBody().getLogin(), orgInfo.getBody().getId());
					return orgRepository.save(newOrg);
				})
			).orElseThrow(() -> new NullPointerException("Org Info Body is null"));
	}
}
