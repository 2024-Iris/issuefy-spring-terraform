package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import site.iris.issuefy.entity.Label;
import site.iris.issuefy.repository.LabelRepository;

import java.util.Optional;

class LabelServiceTest {

	@Mock
	private LabelRepository labelRepository;

	@InjectMocks
	private LabelService labelService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@DisplayName("DB에 존재하지 않은 레이블은 생성한다.")
	@Test
	void findOrCreateLabel_Create() {
		// given
		String labelName = "new label name";
		String labelColor = "fffafa";
		Label newLabel = Label.of(labelName, labelColor);

		// 레이블이 존재하지 않을 때
		when(labelRepository.findByNameAndColor(labelName, labelColor)).thenReturn(Optional.empty());
		when(labelRepository.save(any(Label.class))).thenReturn(newLabel);

		// when
		Label result = labelService.findOrCreateLabel(labelName, labelColor);

		// then
		assertNotNull(result);
		assertEquals(labelName, result.getName());
		assertEquals(labelColor, result.getColor());
	}

	@DisplayName("DB에 존재하는 레이블은 생성하지 않는다.")
	@Test
	void findOrCreateLabel_Find() {
		// given
		String labelName = "existing label name";
		String labelColor = "000000";
		Label existingLabel = Label.of(labelName, labelColor);

		// 레이블이 이미 존재할 때
		when(labelRepository.findByNameAndColor(labelName, labelColor)).thenReturn(Optional.of(existingLabel));

		// when
		Label result = labelService.findOrCreateLabel(labelName, labelColor);

		// then
		assertNotNull(result);
		assertEquals(labelName, result.getName());
		assertEquals(labelColor, result.getColor());
	}
}
