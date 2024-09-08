package site.iris.issuefy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import site.iris.issuefy.entity.Issue;
import site.iris.issuefy.model.dto.IssueDto;

@Mapper
public interface IssueMapper {
	IssueMapper INSTANCE = Mappers.getMapper(IssueMapper.class);

	void updateIssueFromDto(IssueDto issueDto, @MappingTarget Issue issue);
}
