package site.iris.issuefy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.iris.issuefy.entity.Label;
import site.iris.issuefy.mapper.LabelMapper;
import site.iris.issuefy.repository.LabelRepository;
import site.iris.issuefy.response.LabelResponse;

@Service
@RequiredArgsConstructor
public class LabelService {
	private final LabelRepository labelRepository;

	public Label findOrCreateLabel(String name, String color) {
		return labelRepository.findByNameAndColor(name, color)
			.orElseGet(() -> {
				Label newLabel = Label.of(name, color);
				return labelRepository.save(newLabel);
			});
	}

	public Optional<List<Label>> getLabelsByIssueId(Long issueId) {
		return labelRepository.findByIssue_id(issueId);
	}

	public List<LabelResponse> convertToResponse(Optional<List<Label>> optionalLabels) {
		return optionalLabels
			.map(labels -> labels.stream()
				.map(LabelMapper.INSTANCE::labelEntityToLabelDto)
				.collect(Collectors.toList()))
			.orElseGet(ArrayList::new);
	}

	public void saveAllLabels(List<Label> labels) {
		labelRepository.saveAll(labels);
	}
}
