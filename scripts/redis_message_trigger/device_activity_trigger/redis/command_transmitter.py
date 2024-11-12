from device_activity_trigger.redis.channels import RedisChannel
from device_activity_trigger.redis.communicator import RedisCommunicator


class CommandTransmitter(RedisCommunicator):

    def __init__(self, logger, redis_host, redis_port, use_ssl: bool = False):
        super().__init__(logger, redis_host, redis_port,
                         outgoing_channel=RedisChannel.DEVICE_ACTIVITY,
                         incoming_channel=RedisChannel.DEVICE_ACTIVITY_RESPONSE,
                         use_ssl=use_ssl)

    def send_command(self, event_content):
        request = event_content
        self._send_request(request)
        self.logger.info(f'Triggered event {request}')
