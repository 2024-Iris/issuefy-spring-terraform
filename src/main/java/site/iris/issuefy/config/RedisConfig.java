package site.iris.issuefy.config;

import java.io.Serializable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import site.iris.issuefy.component.SseEmitters;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
		RedisTemplate<String, Serializable> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		return template;
	}

	@Bean
	public RedisMessageListenerContainer container(LettuceConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic());
        return container;
    }

	@Bean
	public MessageListenerAdapter listenerAdapter(SseEmitters sseEmitters) {
        return new MessageListenerAdapter(sseEmitters, "receiveMessage");
    }

	@Bean
	public ChannelTopic topic() {
        return new ChannelTopic("sse:messages");
    }

}

