package site.iris.issuefy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import site.iris.issuefy.entity.Label;
import site.iris.issuefy.response.LabelResponse;

@Mapper
public interface LabelMapper {
	LabelMapper INSTANCE = Mappers.getMapper(LabelMapper.class);

	LabelResponse labelEntityToLabelDto(Label Label);
}
