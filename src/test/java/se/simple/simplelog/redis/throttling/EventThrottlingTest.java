package se.simple.simplelog.redis.throttling;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.redis.message.DeviceActivityMessage;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventThrottlingTest {

    @Test
    public void givenEventWithoutThrottling_IsThrottled_ShouldReturnFalse() {
        // Given
        int NO_OF_REPETITIONS = 11;
        String LOG_TYPE = "testLogType";
        String DEVICE = "testDevice";

        ThrottlingConfiguration.ThrottlingSetting settingsMock = mock(ThrottlingConfiguration.ThrottlingSetting.class);
        DeviceActivityMessage.EventMessage messageMock = setupMessageMock(LOG_TYPE, DEVICE);

        when(settingsMock.hasSecondsLimit(eq(LOG_TYPE))).thenReturn(false);

        EventThrottling classUnderTest = new EventThrottling(settingsMock);

        for (int i = 0; i < NO_OF_REPETITIONS; i++) {
            // When
            boolean isThrottled = classUnderTest.isThrottled(messageMock);

            // Then
            Assertions.assertFalse(isThrottled);
        }
    }

    @Test
    public void givenEventWithThrottling_IsThrottled_ShouldStartThrottlingAfterFirstEvent() {
        // Given
        int NO_OF_REPETITIONS = 6;
        int SECONDS_LIMIT = 60;
        String LOG_TYPE = "testLogType";
        String DEVICE = "testDevice";

        ThrottlingConfiguration.ThrottlingSetting settingsMock = mock(ThrottlingConfiguration.ThrottlingSetting.class);
        DeviceActivityMessage.EventMessage messageMock = setupMessageMock(LOG_TYPE, DEVICE);

        when(settingsMock.hasSecondsLimit(eq(LOG_TYPE))).thenReturn(true);
        when(settingsMock.getSecondsLimit(eq(LOG_TYPE))).thenReturn(SECONDS_LIMIT);

        EventThrottling classUnderTest = new EventThrottling(settingsMock);

        for (int i = 0; i < NO_OF_REPETITIONS; i++) {
            // When
            boolean isThrottled = classUnderTest.isThrottled(messageMock);

            // Then
            Assertions.assertEquals(i > 0, isThrottled);
        }
    }

    @Test
    public void givenThrottledEventsForMultipleDevices_IsThrottled_ThrottlingShouldBeDeviceIndependent() {
        // Given
        int NO_OF_DEVICES = 5;
        int NO_OF_REPETITIONS_PER_DEVICE = 3;
        int SECONDS_LIMIT = 1;
        String LOG_TYPE = "testLogType";
        String DEVICE_BASE_NAME = "testDevice";

        ThrottlingConfiguration.ThrottlingSetting settingsMock = mock(ThrottlingConfiguration.ThrottlingSetting.class);

        when(settingsMock.hasSecondsLimit(eq(LOG_TYPE))).thenReturn(true);
        when(settingsMock.getSecondsLimit(eq(LOG_TYPE))).thenReturn(SECONDS_LIMIT);

        EventThrottling classUnderTest = new EventThrottling(settingsMock);

        for (int deviceIdx = 0; deviceIdx < NO_OF_DEVICES; deviceIdx++) {
            String deviceName = DEVICE_BASE_NAME + deviceIdx;
            DeviceActivityMessage.EventMessage messageMock = setupMessageMock(LOG_TYPE, deviceName);
            for (int repetition = 0; repetition < NO_OF_REPETITIONS_PER_DEVICE; repetition++) {
                // When
                boolean isThrottled = classUnderTest.isThrottled(messageMock);

                // Then
                Assertions.assertEquals(repetition > 0, isThrottled);
            }
        }
    }

    @Test
    public void givenDifferentEventTypes_IsThrottled_ThrottlingShouldBeLogTypeIndependent() {
        // Given
        int NO_OF_LOG_TYPES = 5;
        int NO_OF_REPETITIONS_PER_DEVICE = 4;
        int SECONDS_LIMIT = 1;
        String LOG_TYPE_BASE = "testLogType";
        String DEVICE = "testDevice";

        ThrottlingConfiguration.ThrottlingSetting settingsMock = mock(ThrottlingConfiguration.ThrottlingSetting.class);

        when(settingsMock.hasSecondsLimit(any())).thenReturn(true);
        when(settingsMock.getSecondsLimit(any())).thenReturn(SECONDS_LIMIT);

        EventThrottling classUnderTest = new EventThrottling(settingsMock);

        for (int logTypeIdx = 0; logTypeIdx < NO_OF_LOG_TYPES; logTypeIdx++) {
            String logType = LOG_TYPE_BASE + logTypeIdx;
            DeviceActivityMessage.EventMessage messageMock = setupMessageMock(DEVICE, logType);
            for (int repetition = 0; repetition < NO_OF_REPETITIONS_PER_DEVICE; repetition++) {
                // When
                boolean isThrottled = classUnderTest.isThrottled(messageMock);

                // Then
                Assertions.assertEquals(repetition > 0, isThrottled);
            }
        }
    }

    @Test
    public void givenTimeDispersedEvent_IsThrottled_ShouldThrottleOnlyWithinTimeLimit() {
        // Given
        int NO_OF_EVENTS = 10;
        int EVENTS_SENT_PER_THROTTLE_WINDOW = 2;
        int SECONDS_LIMIT = 20;
        String LOG_TYPE = "testLogType";
        String DEVICE = "testDevice";

        ThrottlingConfiguration.ThrottlingSetting settingsMock = mock(ThrottlingConfiguration.ThrottlingSetting.class);

        when(settingsMock.hasSecondsLimit(any())).thenReturn(true);
        when(settingsMock.getSecondsLimit(any())).thenReturn(SECONDS_LIMIT);

        EventThrottling classUnderTest = new EventThrottling(settingsMock);

        LocalDateTime eventTime = LocalDateTime.now();

        for (int eventIdx = 0; eventIdx < NO_OF_EVENTS; eventIdx++) {
            DeviceActivityMessage.EventMessage messageMock = setupMessageMock(DEVICE, LOG_TYPE, eventTime);

            // When
            boolean isThrottled = classUnderTest.isThrottled(messageMock);

            // Then
            Assertions.assertEquals(eventIdx % EVENTS_SENT_PER_THROTTLE_WINDOW != 0, isThrottled);

            eventTime = eventTime.plusSeconds(SECONDS_LIMIT / EVENTS_SENT_PER_THROTTLE_WINDOW + 1);
        }

    }

    private DeviceActivityMessage.EventMessage setupMessageMock(String logType, String deviceName) {
        return setupMessageMock(logType, deviceName, LocalDateTime.now());
    }

    private DeviceActivityMessage.EventMessage setupMessageMock(String logType, String deviceName,
                                                                LocalDateTime eventTime) {
        DeviceActivityMessage.EventMessage messageMock = mock(DeviceActivityMessage.EventMessage.class);
        SimpleLog eventMock = mock(SimpleLog.class);

        when(messageMock.getEvent()).thenReturn(eventMock);
        when(eventMock.getLogType()).thenReturn(logType);
        when(eventMock.getDevice()).thenReturn(deviceName);
        when(eventMock.getTime()).thenReturn(eventTime);

        return messageMock;
    }

}
