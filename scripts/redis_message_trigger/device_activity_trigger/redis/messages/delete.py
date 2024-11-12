from device_activity_trigger.redis.messages.message import RedisMessage, MessageType
from device_activity_trigger.utils.json.parameter import JsonParameter
from device_activity_trigger.utils.json.section import JsonSection
from device_activity_trigger.utils.json.section_parameter import JsonSectionParameter


class ParameterNames(object):
    FILTER = "filter"
    DEVICE = "device"


class Filter(JsonSection):

    def __init__(self, device):
        self.device = device

    @classmethod
    def _get_json_parameters_list(cls) -> list[JsonParameter]:
        return [JsonParameter(ParameterNames.DEVICE, True)]


class DeleteMessage(RedisMessage):

    def __init__(self, device):
        super().__init__(MessageType.DELETE)

        self.filter = Filter(device)

    @classmethod
    def _get_json_parameters_list(cls) -> list[JsonParameter]:
        json_parameters = super()._get_json_parameters_list()
        json_parameters.extend([JsonSectionParameter(ParameterNames.FILTER, Filter, is_required=True)])

        return json_parameters
