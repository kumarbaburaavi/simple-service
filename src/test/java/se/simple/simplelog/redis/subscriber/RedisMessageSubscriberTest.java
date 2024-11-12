package se.simple.simplelog.redis.subscriber;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.redis.message.DeleteFilter;
import se.simple.simplelog.redis.message.DeviceActivityMessage;
import se.simple.simplelog.redis.message.MessageInterpreter;
import se.simple.simplelog.redis.throttling.EventThrottling;
import se.simple.simplelog.service.LogService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisMessageSubscriberTest {

    @Mock
    MessageInterpreter messageInterpreterMock;

    @Mock
    LogService logServiceMock;

    @Mock
    EventThrottling eventThrottlingMock;

    @Test
    void givenInvalidMessage_WhenOnMessage_ShouldInterpretButNotStore() {
        // Given
        String MESSAGE_STRING = "invalid_test_message";
        Message message = mock(Message.class);
        SimpleLog simpleLog = mock(SimpleLog.class);
        when(message.toString()).thenReturn(MESSAGE_STRING);
        when(messageInterpreterMock.interpretMessage(MESSAGE_STRING)).thenReturn(null);

        // When
        RedisMessageSubscriber subscriber =
                new RedisMessageSubscriber(messageInterpreterMock, logServiceMock, eventThrottlingMock);
        subscriber.onMessage(message, null);

        // Then
        verify(messageInterpreterMock, times(1)).interpretMessage(MESSAGE_STRING);
        verify(logServiceMock, never()).store(simpleLog);
        verify(eventThrottlingMock, never()).isThrottled(any());
    }

    @Test
    void givenValidEventMessage_WhenOnMessage_ShouldInterpretAndStore() {
        // Given
        String MESSAGE_STRING = "test_message";
        Message message = mock(Message.class);
        DeviceActivityMessage.EventMessage deviceActivityMessage = mock(DeviceActivityMessage.EventMessage.class);
        SimpleLog simpleLog = mock(SimpleLog.class);
        when(message.toString()).thenReturn(MESSAGE_STRING);
        when(deviceActivityMessage.getEvent()).thenReturn(simpleLog);
        when(messageInterpreterMock.interpretMessage(MESSAGE_STRING)).thenReturn(deviceActivityMessage);
        when(eventThrottlingMock.isThrottled(any())).thenReturn(false);

        // When
        RedisMessageSubscriber subscriber =
                new RedisMessageSubscriber(messageInterpreterMock, logServiceMock, eventThrottlingMock);
        subscriber.onMessage(message, null);

        // Then
        verify(messageInterpreterMock, times(1)).interpretMessage(MESSAGE_STRING);
        verify(logServiceMock, times(1)).store(simpleLog);
        verify(eventThrottlingMock, times(1)).isThrottled(deviceActivityMessage);
    }

    @Test
    void givenValidEventMessageAndThrottleLevelReached_WhenOnMessage_ShouldInterpretAndNotStore() {
        // Given
        String MESSAGE_STRING = "test_message";
        Message message = mock(Message.class);
        DeviceActivityMessage.EventMessage deviceActivityMessage = mock(DeviceActivityMessage.EventMessage.class);
        SimpleLog simpleLog = mock(SimpleLog.class);
        when(message.toString()).thenReturn(MESSAGE_STRING);
        when(messageInterpreterMock.interpretMessage(MESSAGE_STRING)).thenReturn(deviceActivityMessage);
        when(eventThrottlingMock.isThrottled(deviceActivityMessage)).thenReturn(true);

        // When
        RedisMessageSubscriber subscriber =
                new RedisMessageSubscriber(messageInterpreterMock, logServiceMock, eventThrottlingMock);
        subscriber.onMessage(message, null);

        // Then
        verify(messageInterpreterMock, times(1)).interpretMessage(MESSAGE_STRING);
        verify(logServiceMock, never()).store(simpleLog);
        verify(eventThrottlingMock, times(1)).isThrottled(deviceActivityMessage);
    }

    @Test
    void givenValidDeleteMessage_WhenOnMessage_ShouldInterpretAndDelete() {
        // Given
        String DEVICE = "test_device";
        String MESSAGE_STRING = "test_delete_message";
        Message message = mock(Message.class);
        DeviceActivityMessage.DeleteMessage deleteMessage = mock(DeviceActivityMessage.DeleteMessage.class);
        DeleteFilter deleteFilter = mock(DeleteFilter.class);
        when(deleteFilter.getDevice()).thenReturn(DEVICE);
        when(message.toString()).thenReturn(MESSAGE_STRING);
        when(deleteMessage.getFilter()).thenReturn(deleteFilter);
        when(messageInterpreterMock.interpretMessage(MESSAGE_STRING)).thenReturn(deleteMessage);

        // When
        RedisMessageSubscriber subscriber =
                new RedisMessageSubscriber(messageInterpreterMock, logServiceMock, eventThrottlingMock);
        subscriber.onMessage(message, null);

        // Then
        verify(messageInterpreterMock, times(1)).interpretMessage(MESSAGE_STRING);
        verify(logServiceMock, never()).store(any());
        verify(logServiceMock, times(1)).delete(eq(DEVICE));
        verify(eventThrottlingMock, never()).isThrottled(any());
    }

}
