package site.iris.issuefy.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import site.iris.issuefy.service.NotificationService;
import site.iris.issuefy.service.SseService;

@Configuration
public class RedisConfig {

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		LettuceConnectionFactory connectionFactory,
		@Qualifier("notificationAdapter") MessageListenerAdapter notificationListenerAdapter,
		@Qualifier("disconnectAdapter") MessageListenerAdapter disconnectListenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(notificationListenerAdapter, notificationsTopic());
		container.addMessageListener(disconnectListenerAdapter, disconnectTopic());
		return container;
	}

	@Bean(name = "notificationAdapter")
	public MessageListenerAdapter listenerAdapter(NotificationService notificationService) {
		return new MessageListenerAdapter(notificationService, "handleRedisMessage");
	}

	@Bean(name = "disconnectAdapter")
	public MessageListenerAdapter disconnectListenerAdapter(SseService sseService) {
		return new MessageListenerAdapter(sseService, "handleDisconnect");
	}

	@Bean
	public ChannelTopic notificationsTopic() {
		return new ChannelTopic("notifications");
	}

	@Bean
	public ChannelTopic disconnectTopic() {
		return new ChannelTopic("disconnect");
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();

		return template;
	}
}