from device_activity_trigger.utils.json.section import JsonSection


class JsonSectionClassSwitch(object):

    def __init__(self, key_parameter_name: str, value_to_class_map: dict[any, JsonSection]):
        self.key_parameter_name = key_parameter_name
        self.value_to_class_map = value_to_class_map

    def is_valid_section_class(self, cls):
        return cls in self.get_section_classes()

    def get_section_classes(self):
        return self.value_to_class_map.values()

    def get_section_class_names(self):
        return [str(cls) for cls in self.get_section_classes()]

    def get_section_class_for_json(self, json_obj: dict):
        if json_obj is None:
            return None

        if self.key_parameter_name not in json_obj:
            raise Exception(f"Key parameter {self.key_parameter_name} not found in json: {json_obj}. "
                            f"Cannot create section class from json!")
        
        key = json_obj[self.key_parameter_name]
        if key not in self.value_to_class_map:
            raise Exception(f"No class matches value {key} of {self.key_parameter_name}. "
                            f"Cannot create section class from json!")

        return self.value_to_class_map[key]
