package se.simple.simplelog.redis.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.simple.simplelog.api.SimpleLog;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "messageType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeviceActivityMessage.EventMessage.class, name = "EVENT"),
        @JsonSubTypes.Type(value = DeviceActivityMessage.DeleteMessage.class, name = "DELETE")
})
public class DeviceActivityMessage {

    String messageType;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class EventMessage extends DeviceActivityMessage {
        SimpleLog event;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class DeleteMessage extends DeviceActivityMessage {
        DeleteFilter filter;

        @Override
        public String toString() {
            return filter.toString();
        }
    }

}
