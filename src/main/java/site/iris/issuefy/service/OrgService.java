package site.iris.issuefy.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.Org;
import site.iris.issuefy.exception.code.ErrorCode;
import site.iris.issuefy.exception.validation.EmptyBodyException;
import site.iris.issuefy.model.dto.GithubOrgDto;
import site.iris.issuefy.repository.OrgRepository;

@Service
@RequiredArgsConstructor
public class OrgService {
	private final OrgRepository orgRepository;

	@Transactional
	public Org saveOrg(ResponseEntity<GithubOrgDto> orgInfo) {
		if (orgInfo.getBody() == null) {
			throw new EmptyBodyException(ErrorCode.ORG_BODY_EMPTY.getMessage(), ErrorCode.ORG_BODY_EMPTY.getStatus());
		}

		GithubOrgDto orgDto = orgInfo.getBody();
		return orgRepository.findByName(orgDto.getLogin())
			.orElseGet(() -> {
				Org newOrg = new Org(orgDto.getLogin(), orgDto.getId());
				return orgRepository.save(newOrg);
			});
	}
}
