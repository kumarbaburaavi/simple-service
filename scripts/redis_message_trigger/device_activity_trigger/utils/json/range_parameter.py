from device_activity_trigger.utils.json.integer_range import IntegerRange
from device_activity_trigger.utils.json.parameter import JsonParameter, JsonParameterException


class JsonRangeParameter(JsonParameter):

    def __init__(self, name, is_required=False, required_value=None, allow_list=True):
        super().__init__(name, is_required=is_required, required_value=required_value, required_type=IntegerRange,
                         allow_list=allow_list)

    def parse(self, value):
        if value is None:
            return None
        if type(value) is str:
            try:
                range_parts = value.split("-")
                return IntegerRange(range_parts[0], range_parts[1])
            except Exception:
                raise JsonParameterException(f"Failed to parse string {value} as a range! "
                                             f"Should be in the format of '<MIN_VALUE>-<MAX_VALUE>'.")
        elif type(value) is int:
            return IntegerRange(value)
        else:
            raise JsonParameterException(f"Failed to parse value {value} as a range! "
                                         f"Should either be an int or a string in format '<MIN_VALUE>-<MAX_VALUE>'.")

    def get_json_values_from_obj(self, json_section):
        integer_ranges = super().get_json_values_from_obj(json_section)
        if integer_ranges is None:
            return None
        elif type(integer_ranges) is list:
            return [integer_range.to_json_value() for integer_range in integer_ranges]
        else:
            return integer_ranges.to_json_value()


