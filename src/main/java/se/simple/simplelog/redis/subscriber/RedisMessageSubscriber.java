package se.simple.simplelog.redis.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import se.simple.simplelog.redis.message.DeviceActivityMessage;
import se.simple.simplelog.redis.message.MessageInterpreter;
import se.simple.simplelog.redis.throttling.EventThrottling;
import se.simple.simplelog.service.LogService;

@Service
public class RedisMessageSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    private final MessageInterpreter messageInterpreter;
    private final LogService logService;
    private final EventThrottling eventThrottling;

    public RedisMessageSubscriber(MessageInterpreter messageInterpreter, LogService logService,
                                  EventThrottling eventThrottling) {
        this.messageInterpreter = messageInterpreter;
        this.logService = logService;
        this.eventThrottling = eventThrottling;
    }

    public void onMessage(Message message, byte[] pattern) {
        logger.info("Message received: {}", message);
        DeviceActivityMessage interpretedMessage = messageInterpreter.interpretMessage(message.toString());
        if (interpretedMessage != null) {
            if (interpretedMessage instanceof DeviceActivityMessage.EventMessage eventMessage) {
                handleEventMessage(eventMessage);
            } else if (interpretedMessage instanceof DeviceActivityMessage.DeleteMessage deleteMessage) {
                handleDeleteMessage(deleteMessage);
            }
        }
    }

    private void handleEventMessage(DeviceActivityMessage.EventMessage eventMessage) {
        if (eventThrottling.isThrottled(eventMessage)) {
            logger.info("Simple log {} throttled. Not storing, since event of this type and device " +
                    "was already received within throttling window.", eventMessage);
        } else {
            logService.store(eventMessage.getEvent());
            logger.info("Triggered storing of simple log {}", eventMessage);
        }
    }

    private void handleDeleteMessage(DeviceActivityMessage.DeleteMessage deleteMessage) {
        logService.delete(deleteMessage.getFilter().getDevice());
        logger.info("Triggered deletion of simple logs for {}", deleteMessage);
    }
}
