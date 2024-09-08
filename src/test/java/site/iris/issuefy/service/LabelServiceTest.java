package site.iris.issuefy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.exception.resource.LabelNotFoundException;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.LabelRepository;
import site.iris.issuefy.response.LabelResponse;

class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private IssueLabelRepository issueLabelRepository;

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

        when(labelRepository.findByNameAndColor(labelName, labelColor)).thenReturn(Optional.empty());
        when(labelRepository.save(any(Label.class))).thenReturn(newLabel);

        // when
        Label result = labelService.findOrCreateLabel(labelName, labelColor);

        // then
        assertNotNull(result);
        assertEquals(labelName, result.getName());
        assertEquals(labelColor, result.getColor());
        verify(labelRepository).save(any(Label.class));
    }

    @DisplayName("DB에 존재하는 레이블은 생성하지 않는다")
    @Test
    void findOrCreateLabel_Find() {
        // given
        String labelName = "existing label name";
        String labelColor = "000000";
        Label existingLabel = Label.of(labelName, labelColor);

        when(labelRepository.findByNameAndColor(labelName, labelColor)).thenReturn(Optional.of(existingLabel));

        // when
        Label result = labelService.findOrCreateLabel(labelName, labelColor);

        // then
        assertNotNull(result);
        assertEquals(labelName, result.getName());
        assertEquals(labelColor, result.getColor());
        verify(labelRepository, never()).save(any(Label.class));
    }

    @DisplayName("이슈에 대한 레이블을 반환한다")
    @Test
    void getLabelsByIssueId_ReturnsLabels() {
        // given
        Long testIssueId = 1L;
        List<IssueLabel> issueLabels = List.of(
            IssueLabel.of(null, Label.of("test label name1", "000000")),
            IssueLabel.of(null, Label.of("test label name2", "111111"))
        );

        when(issueLabelRepository.findByIssueId(testIssueId)).thenReturn(Optional.of(issueLabels));

        // when
        List<Label> result = labelService.getLabelsByIssueId(testIssueId);

        // then
        assertEquals(2, result.size());
        assertEquals("test label name1", result.get(0).getName());
        assertEquals("test label name2", result.get(1).getName());
    }

    @DisplayName("이슈에 대한 레이블이 없다면 Label 예외가 발생한다.")
    @Test
    void getLabelsByIssueId_ThrowsException() {
        // given
        Long testIssueId = 55L;

        when(issueLabelRepository.findByIssueId(testIssueId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(LabelNotFoundException.class, () -> labelService.getLabelsByIssueId(testIssueId));
        verify(issueLabelRepository).findByIssueId(testIssueId);
    }

    @DisplayName("레이블을 Response DTO로 변환한다")
    @Test
    void convertLabelsResponse_ReturnsDto() {
        // given
        List<Label> labels = List.of(
            Label.of("test label name1", "000000"),
            Label.of("test label name2", "111111")
        );

        // when
        List<LabelResponse> result = labelService.convertLabelsResponse(labels);

        // then
        assertEquals(labels.size(), result.size());
        assertEquals("test label name1", result.get(0).getName());
        assertEquals("000000", result.get(0).getColor());
        assertEquals("test label name2", result.get(1).getName());
        assertEquals("111111", result.get(1).getColor());
    }

    @DisplayName("이슈에 대한 모든 레이블을 저장한다")
    @Test
    void saveAllLabels() {
        // given
        List<Label> labels = List.of(
            Label.of("test label name1", "000000"),
            Label.of("test label name2", "111111")
        );

        // when
        labelService.saveAllLabels(labels);

        // then
        verify(labelRepository, times(1)).saveAll(labels);
    }
}