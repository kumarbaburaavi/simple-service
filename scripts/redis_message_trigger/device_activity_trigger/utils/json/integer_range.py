import json

from device_activity_trigger.utils.random_generator import generate_int_in_range


class IntegerRange(object):

    def __init__(self, min_value, max_value=None):
        self.min_value = int(min_value)
        self.max_value = self.min_value if max_value is None else int(max_value)

    def get_value(self):
        if self.min_value == self.max_value:
            return self.min_value
        else:
            return generate_int_in_range(self.min_value, self.max_value)

    def to_json_value(self) -> int | str:
        if self.max_value is None:
            return self.min_value
        elif self.min_value is None or self.min_value == self.max_value:
            return self.max_value

        return f"{self.min_value}-{self.max_value}"

    def __str__(self):
        return str(self.to_json_value())
