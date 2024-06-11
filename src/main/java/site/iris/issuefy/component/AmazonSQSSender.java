package site.iris.issuefy.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Component
@Slf4j
public class AmazonSQSSender {

	private final SqsTemplate template;

	@Value("${application.amazon.sqs.queue-name}")
	private String queueName;

	public AmazonSQSSender(SqsAsyncClient sqsAsyncClient) {
		this.template = SqsTemplate.newTemplate(sqsAsyncClient);
	}

	public void sendMessage(String message) {
		log.info("Sending message: {}", message);
		template.send(to -> to
			.queue(queueName)
			.payload(message));
	}

}
