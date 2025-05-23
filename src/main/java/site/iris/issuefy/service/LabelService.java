package site.iris.issuefy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.IssueLabel;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.eums.ErrorCode;
import site.iris.issuefy.exception.resource.LabelNotFoundException;
import site.iris.issuefy.mapper.LabelMapper;
import site.iris.issuefy.repository.IssueLabelRepository;
import site.iris.issuefy.repository.LabelRepository;
import site.iris.issuefy.response.LabelResponse;

@Service
@RequiredArgsConstructor
public class LabelService {
	private final LabelRepository labelRepository;
	private final IssueLabelRepository issueLabelRepository;

	public Label findOrCreateLabel(String name, String color) {
		return labelRepository.findByNameAndColor(name, color).orElseGet(() -> {
			Label newLabel = Label.of(name, color);
			return labelRepository.save(newLabel);
		});
	}

	public List<Label> getLabelsByIssueId(Long issueId) {
		ErrorCode errorCode = ErrorCode.NOT_EXIST_LABEL;
		return issueLabelRepository.findByIssueId(issueId)
			.orElseThrow(() -> new LabelNotFoundException(errorCode.getMessage(), errorCode.getStatus(),
				String.valueOf(issueId))).stream().map(IssueLabel::getLabel).toList();
	}

	public List<LabelResponse> convertLabelsResponse(List<Label> labelResult) {
		return labelResult.stream().map(LabelMapper.INSTANCE::labelEntityToLabelDto).toList();
	}

	public void saveAllLabels(List<Label> labels) {
		labelRepository.saveAll(labels);
	}
}
