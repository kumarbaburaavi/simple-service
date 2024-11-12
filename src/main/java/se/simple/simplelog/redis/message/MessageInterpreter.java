package se.simple.simplelog.redis.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageInterpreter {

    private static final Logger logger = LoggerFactory.getLogger(MessageInterpreter.class);
    private final ObjectMapper objectMapper;

    public MessageInterpreter() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public DeviceActivityMessage interpretMessage(String jsonMessage) {
        try {
            return objectMapper.readValue(jsonMessage, DeviceActivityMessage.class);
        } catch (JsonProcessingException e) {
            logger.warn("Could not interpret incoming message {}!", jsonMessage, e);
            return null;
        }
    }
}
