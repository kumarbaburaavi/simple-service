from device_activity_trigger.utils.string_utils import to_camel_case


class JsonParameterException(Exception):
    """Raised when encoding of a JsonSection object to json string fails"""
    pass


class JsonParameter(object):

    def __init__(self, name, is_required=False, required_value=None, required_type=None, allow_list=True):
        self.name = name
        self.is_required = is_required
        self.required_value = required_value
        self.required_type = required_type
        self.allow_list = allow_list

    def is_value_valid(self, value):
        if self.is_required and value is None:
            return False
        elif self.required_value is not None and self.required_value != value:
            return False
        elif self.required_type is not None and type(value) is not self.required_type:
            return False
        return True

    def _validate_value(self, parameter_value):
        if self.is_required and parameter_value is None:
            raise JsonParameterException(f'Json parameter {self.name} is required!')
        elif self.required_value is not None and self.required_value != parameter_value:
            raise JsonParameterException(f'Invalid value {parameter_value} for json parameter {self.name}! '
                                         f'Required value: {self.required_value}')
        elif (parameter_value is not None and self.required_type is not None
              and type(parameter_value) is not self.required_type):
            raise JsonParameterException(f'Invalid type of {self.name}: {type(parameter_value)}! '
                                         f'Required type is {self.required_type}.')
        return parameter_value

    def parse(self, value):
        # to be overridden in subclass if other type of parsing wanted
        return value

    def get_json_values_from_obj(self, json_section):
        return self._validate_value(getattr(json_section, self.name))

    def get_values_from_dict(self, json_dict):
        value = json_dict.get(to_camel_case(self.name))
        if type(value) is list:
            if not self.allow_list:
                raise JsonParameterException(f'Json parameter {self.name} is not allowed to contain lists!')
            return [self._validate_value(self.parse(val)) for val in value]
        else:
            parsed_value = self.parse(value)
            return self._validate_value(parsed_value)
