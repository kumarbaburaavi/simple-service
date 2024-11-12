package se.simple.simplelog.redis.throttling;

import org.springframework.stereotype.Component;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.redis.message.DeviceActivityMessage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventThrottling {

    private final ThrottlingConfiguration.ThrottlingSetting throttlingSetting;
    private final Map<String, DeviceThrottle> deviceThrottles = new HashMap<>();

    public EventThrottling(ThrottlingConfiguration.ThrottlingSetting throttlingSetting) {
        this.throttlingSetting = throttlingSetting;
    }

    public boolean isThrottled(DeviceActivityMessage.EventMessage eventMessage) {
        SimpleLog event = eventMessage.getEvent();

        if (!throttlingSetting.hasSecondsLimit(event.getLogType())) {
            return false;
        }

        String device = event.getDevice();
        if (!deviceThrottles.containsKey(event.getDevice())) {
            deviceThrottles.put(device, new DeviceThrottle(throttlingSetting));
        }

        return deviceThrottles.get(device).isThrottled(event);
    }

    private static class DeviceThrottle {

        private final ThrottlingConfiguration.ThrottlingSetting throttlingSetting;
        private final Map<String, LocalDateTime> lastNonThrottledEvent = new HashMap<>();

        public DeviceThrottle(ThrottlingConfiguration.ThrottlingSetting throttlingSetting) {
            this.throttlingSetting = throttlingSetting;
        }

        public boolean isThrottled(SimpleLog event) {
            String eventType = event.getLogType();
            LocalDateTime currentEventTime = event.getTime();

            if (!lastNonThrottledEvent.containsKey(eventType)) {
                lastNonThrottledEvent.put(eventType, currentEventTime);
                return false;
            }

            LocalDateTime lastNonThrottledTime = lastNonThrottledEvent.get(eventType);
            boolean throttleTimeExpired =
                    currentEventTime.minusSeconds(throttlingSetting.getSecondsLimit(eventType))
                            .isAfter(lastNonThrottledTime);
            if (throttleTimeExpired) {
                lastNonThrottledEvent.put(eventType, currentEventTime);
                return false;
            } else {
                return true;
            }
        }
    }
}
