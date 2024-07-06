package site.iris.issuefy.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import site.iris.issuefy.entity.Label;
import site.iris.issuefy.response.LabelResponse;

class LabelMapperTest {

	@DisplayName("Label Entity를 DTO로 변환한다")
	@Test
	void labelEntityToLabelDto() {
		// given
		Label testLabel = Label.of("good first issue", "0e8a16");

		// when
		LabelResponse response = LabelMapper.INSTANCE.labelEntityToLabelDto(testLabel);

		// then
		assertEquals(testLabel.getName(), response.getName());
		assertEquals(testLabel.getColor(), response.getColor());
	}
}