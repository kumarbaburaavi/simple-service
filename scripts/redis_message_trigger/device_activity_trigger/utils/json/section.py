import json
from io import TextIOWrapper

from device_activity_trigger.utils.json.parameter import JsonParameter, JsonParameterException
from device_activity_trigger.utils.string_utils import to_camel_case, to_snake_case


class JsonSectionEncodeException(Exception):
    """Raised when encoding of a JsonSection object to json string fails"""
    pass


class JsonSectionDecodeException(Exception):
    """Raised when decoding a json string to a JsonSection fails"""
    pass


class JsonSection(object):

    @classmethod
    def _get_json_parameters(cls) -> dict[str, JsonParameter]:
        return {parameter.name: parameter for parameter in cls._get_json_parameters_list()}

    @classmethod
    def _get_json_parameters_list(cls) -> list[JsonParameter]:
        raise NotImplementedError("'_get_json_parameters_list' must be implemented in subclass of JsonSection!")

    @classmethod
    def _get_json_parameter(cls, parameter_name) -> JsonParameter | None:
        parameter_list = cls._get_json_parameters_list()
        if parameter_list is not None and parameter_name in parameter_list:
            return parameter_list[parameter_name]
        else:
            return None

    def get_json_dict(self):
        json_dict = {}
        for parameter_name, parameter in self._get_json_parameters().items():
            parameter_value = parameter.get_json_values_from_obj(self)
            if parameter_value is not None:
                json_dict[to_camel_case(parameter_name)] = parameter_value
        return json_dict

    def to_json(self):
        try:
            json_str = json.dumps(self.get_json_dict())
        except Exception as e:
            raise JsonSectionEncodeException(f'Failed to encode {type(self)} to JSON string! Exception: {e}')
        return json_str

    @classmethod
    def from_json(cls, json_input):
        try:
            if type(json_input) is str:
                json_dict = json.loads(json_input)
            elif type(json_input) is TextIOWrapper:
                json_dict = json.load(json_input)
            elif type(json_input) is dict:
                json_dict = json_input
            else:
                raise JsonSectionDecodeException(f'Invalid input type {type(json_input)} to from_json-function!')

            json_section = cls()
            for parameter_name, parameter in json_section._get_json_parameters().items():
                parameter_value = parameter.get_values_from_dict(json_dict)
                if parameter_value is not None:
                    setattr(json_section, to_snake_case(parameter_name), parameter_value)
            return json_section
        except (JsonParameterException, TypeError, JsonSectionDecodeException) as e:
            raise JsonSectionDecodeException(f'Failed to decode from JSON:  {json_input}! Exception {e}')

    def __str__(self):
        return self.to_json()
