from datetime import datetime
from enum import Enum

from device_activity_trigger.redis.messages.message import RedisMessage, MessageType, _interpret_to_string
from device_activity_trigger.utils.json.parameter import JsonParameter
from device_activity_trigger.utils.json.section import JsonSection
from device_activity_trigger.utils.json.section_parameter import JsonSectionParameter


class ParameterNames(object):
    EVENT = "event"
    LOG_TYPE = "log_type"
    DEVICE = "device"
    TIME = "time"


class EventType(Enum):
    DEVICE_ASSIGNED = "DEVICE_ASSIGNED"

    def __str__(self):
        return self.value


class Event(JsonSection):

    def __init__(self, log_type, device, time):
        self.log_type = _interpret_to_string(log_type)
        self.device = device
        self.time = time.isoformat()

    @classmethod
    def _get_json_parameters_list(cls) -> list[JsonParameter]:
        return [JsonParameter(ParameterNames.LOG_TYPE, True),
                JsonParameter(ParameterNames.DEVICE, True),
                JsonParameter(ParameterNames.TIME, True)]


class EventMessage(RedisMessage):

    def __init__(self, log_type, device):
        super().__init__(MessageType.EVENT)

        self.event = Event(log_type, device, datetime.utcnow())

    @classmethod
    def _get_json_parameters_list(cls) -> list[JsonParameter]:
        json_parameters = super()._get_json_parameters_list()
        json_parameters.extend([JsonSectionParameter(ParameterNames.EVENT, Event, is_required=True)])

        return json_parameters
