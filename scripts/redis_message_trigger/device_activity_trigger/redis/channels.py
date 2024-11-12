from enum import Enum


class RedisChannel(Enum):
    DEVICE_ACTIVITY = "device_activity"
    DEVICE_ACTIVITY_RESPONSE = "device_activity_response"
