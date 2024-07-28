package site.iris.issuefy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import site.iris.issuefy.model.dto.BlacklistedJwtDto;
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

	@Bean
	public RedisTemplate<String, BlacklistedJwtDto> blacklistedJwtRedisTemplate(
		LettuceConnectionFactory connectionFactory) {
		RedisTemplate<String, BlacklistedJwtDto> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<BlacklistedJwtDto> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
			BlacklistedJwtDto.class);

		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();

		return template;
	}
}

