package site.iris.issuefy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import site.iris.issuefy.model.dto.IssueSubscriptionDto;
import site.iris.issuefy.model.vo.IssueSubscriptionVo;

@Mapper
public interface IssueMapper {
	IssueMapper INSTANCE = Mappers.getMapper(IssueMapper.class);

	IssueSubscriptionDto issueVoToIssueDto(IssueSubscriptionVo issueSubscriptionVo);
}
