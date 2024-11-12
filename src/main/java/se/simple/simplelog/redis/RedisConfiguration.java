package se.simple.simplelog.redis;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import se.simple.simplelog.redis.message.MessageInterpreter;
import se.simple.simplelog.redis.subscriber.RedisMessageSubscriber;
import se.simple.simplelog.redis.throttling.EventThrottling;
import se.simple.simplelog.service.LogService;


@Configuration
@Getter
public class RedisConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    @Value("${spring.data.redis.host}")
    private String hostName;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${redis.incoming.event.channel}")
    private String channelName;

    @Value("${redis.disabled:false}")
    private boolean isRedisDisabled;

    @Autowired
    private LogService logService;

    @Autowired
    private EventThrottling eventThrottling;

    @Bean
    MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(new RedisMessageSubscriber(messageInterpreter(), logService, eventThrottling));
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        if (isRedisDisabled) {
            logger.warn("Redis disabled. Not setting up any listener.");
            return null;
        }
        RedisMessageListenerContainer container;
        try {
            container = new RedisMessageListenerContainer();
            JedisConnectionFactory connectionFactory = jedisConnectionFactory();
            ChannelTopic channelTopic = topic();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(messageListener(), channelTopic);
            logger.info("Redis message listener setup. Host: {}, Port: {}, Channel: {}",
                    connectionFactory.getHostName(), connectionFactory.getPort(), channelTopic.getTopic());
        } catch (Exception e) {
            logger.error("Redis message listener setup failed", e);
            container = null;
        }
        return container;
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic(channelName);
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration(hostName, port);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    MessageInterpreter messageInterpreter() {
        return new MessageInterpreter();
    }

}
