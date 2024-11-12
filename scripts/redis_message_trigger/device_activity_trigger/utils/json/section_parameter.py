from device_activity_trigger.utils.json.parameter import JsonParameter, JsonParameterException
from device_activity_trigger.utils.json.section import JsonSection
from device_activity_trigger.utils.json.section_class_switch import JsonSectionClassSwitch


class JsonSectionParameter(JsonParameter):

    def __init__(self, name,
                 json_section_class=None,
                 json_section_class_switch: JsonSectionClassSwitch = None,
                 is_required=False):
        super().__init__(name, is_required=is_required)
        self.json_section_class = json_section_class
        self.json_section_class_switch = json_section_class_switch

    def _convert_section_to_json(self, section):
        if section is None:
            raise JsonParameterException(f'Section was None. Cannot convert {self.name} to JSON!')
        elif type(section) is str:
            # try converting to correct class
            section = self.json_section_class.from_json(section)
        elif self.json_section_class is not None and type(section) is not self.json_section_class:
            raise JsonParameterException(f'Invalid class of parameter {self.name}. Was {type(section)}, '
                                         f'but should have been {self.json_section_class}!')
        elif (self.json_section_class_switch is not None
              and not self.json_section_class_switch.is_valid_section_class(type(section))):
            raise JsonParameterException(f'Invalid class of parameter {self.name}. Was {type(section)}, but should have '
                                         f'been any of {".,".join(self.json_section_class_switch.get_section_class_names())}!')

        return section.get_json_dict()

    def parse(self, value):
        if value is None:
            return None

        if self.json_section_class is not None:
            json_section_class = self.json_section_class
        elif self.json_section_class_switch is not None:
            json_section_class = self.json_section_class_switch.get_section_class_for_json(value)
        else:
            raise Exception(f"Invalid setup of JsonSectionParameter. Neither 'json_section_class' nor "
                            f"'json_section_class_switch' defined! Cannot parse json.")

        if json_section_class is None:
            return None

        return json_section_class.from_json(value)

    def get_json_values_from_obj(self, json_section):
        sections = super().get_json_values_from_obj(json_section)
        if sections is None:
            return None
        elif type(sections) is list:
            return [self._convert_section_to_json(section) for section in sections]
        else:
            return self._convert_section_to_json(sections)
