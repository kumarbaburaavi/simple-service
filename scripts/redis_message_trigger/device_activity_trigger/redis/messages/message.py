import json
from enum import Enum

from device_activity_trigger.utils.json.parameter import JsonParameter
from device_activity_trigger.utils.json.section import JsonSection


class ParameterNames(object):
    MESSAGE_TYPE = "message_type"


class MessageType(Enum):
    EVENT = "EVENT"
    DELETE = "DELETE"
    INVALID = "INVALID"

    def __str__(self):
        return self.value


def _interpret_to_string(value):
    if value is None or type(value) is str:
        return value
    else:
        return value.value


def _format_message_content(content_string):
    try:
        json_obj = json.loads(content_string)
        return json.dumps(json_obj)
    except (TypeError, ValueError):
        return content_string


class RedisMessage(JsonSection):

    def __init__(self, message_type=None):
        self.message_type = _interpret_to_string(message_type)

    @classmethod
    def _get_json_parameters_list(cls) -> list[JsonParameter]:
        return [JsonParameter(ParameterNames.MESSAGE_TYPE, True)]
