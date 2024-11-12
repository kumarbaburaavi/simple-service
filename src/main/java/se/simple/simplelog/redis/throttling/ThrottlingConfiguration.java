package se.simple.simplelog.redis.throttling;

import lombok.Getter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
public class ThrottlingConfiguration {


    @Bean
    @ConfigurationProperties(prefix="redis.event-throttle")
    public ThrottlingSetting throttlingSetting() {
        return new ThrottlingSetting();
    }

    @Getter
    public static class ThrottlingSetting {

        private final Map<String, Integer> seconds = new HashMap<>();

        public boolean hasSecondsLimit(String eventType) {
            return seconds.containsKey(eventType);
        }

        public Integer getSecondsLimit(String eventType) {
            return seconds.get(eventType);
        }

    }

}
