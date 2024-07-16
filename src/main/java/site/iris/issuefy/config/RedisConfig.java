package site.iris.issuefy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import site.iris.issuefy.service.NotificationService;

@Configuration
public class RedisConfig {

	@Bean
	public RedisMessageListenerContainer container(LettuceConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, sseMessagesTopic());
		container.addMessageListener(listenerAdapter, repositoryUpdatesTopic());
		return container;
	}

	@Bean
	public MessageListenerAdapter listenerAdapter(NotificationService notificationService) {
		return new MessageListenerAdapter(notificationService, "handleRedisMessage");
	}

	@Bean
	public ChannelTopic sseMessagesTopic() {
		return new ChannelTopic("sse:messages");
	}

	@Bean
	public PatternTopic repositoryUpdatesTopic() {
		return new PatternTopic("repository_updates");
	}

}

