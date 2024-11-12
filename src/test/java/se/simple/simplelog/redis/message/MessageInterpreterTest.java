package se.simple.simplelog.redis.message;

import org.junit.Assert;
import org.junit.Test;
import se.simple.simplelog.api.SimpleLog;

import java.time.LocalDateTime;

public class MessageInterpreterTest {

    @Test
    public void givenValidEventJsonMessage_WhenInterpreting_ShouldReturnEventMessageWithSimpleLog() {
        // Given
        String jsonMessage = "{\"messageType\": \"EVENT\", \"event\": {\"time\": \"2024-04-12T13:45:30\", \"device\": \"Device1\", \"logType\": \"DEVICE_ASSIGNED\"}}";
        MessageInterpreter messageInterpreter = new MessageInterpreter();

        // When
        DeviceActivityMessage interpretedMessage = messageInterpreter.interpretMessage(jsonMessage);

        // Then
        Assert.assertNotNull(interpretedMessage);
        Assert.assertTrue(interpretedMessage instanceof DeviceActivityMessage.EventMessage);
        SimpleLog simpleLog = ((DeviceActivityMessage.EventMessage) interpretedMessage).getEvent();
        Assert.assertNotNull(simpleLog);
        Assert.assertEquals("Device1", simpleLog.getDevice());
        Assert.assertEquals("DEVICE_ASSIGNED", simpleLog.getLogType());
        Assert.assertEquals(LocalDateTime.parse("2024-04-12T13:45:30"), simpleLog.getTime());
    }

    @Test
    public void givenValidDeleteJsonMessage_WhenInterpreting_ShouldReturnDeleteMessageWithDeleteFilter() {
        // Given
        String jsonMessage = "{\"messageType\": \"DELETE\", \"filter\": {\"device\": \"Device2\"}}";
        MessageInterpreter messageInterpreter = new MessageInterpreter();

        // When
        DeviceActivityMessage interpretedMessage = messageInterpreter.interpretMessage(jsonMessage);

        // Then
        Assert.assertNotNull(interpretedMessage);
        Assert.assertTrue(interpretedMessage instanceof DeviceActivityMessage.DeleteMessage);
        DeleteFilter filter = ((DeviceActivityMessage.DeleteMessage) interpretedMessage).getFilter();
        Assert.assertNotNull(filter);
        Assert.assertEquals("Device2", filter.getDevice());
    }

    @Test
    public void givenInvalidJsonMessage_WhenInterpreting_ShouldReturnNull() {
        // Given
        String jsonMessage = "{\"messageType\": \"EVENT\", \"event\": {\"invalidTimeHeader\": \"2024-04-12T13:45:30\", \"device\": \"Device1\", \"logType\": \"DEVICE_ASSIGNED\"}}";
        MessageInterpreter messageInterpreter = new MessageInterpreter();

        // When
        DeviceActivityMessage interpretedMessage = messageInterpreter.interpretMessage(jsonMessage);

        // Then
        Assert.assertNull(interpretedMessage);
    }
}
