from redis import Redis


def _get_channel_name(channel):
    if channel is None or type(channel) is str:
        return channel
    else:
        return channel.value


class RedisCommunicator(object):

    def __init__(self, logger, redis_host, redis_port, outgoing_channel=None, incoming_channel=None,
                 use_ssl: bool = False):
        self.logger = logger

        self.redis_host = redis_host
        self.redis_port = redis_port

        self.outgoing_channel = _get_channel_name(outgoing_channel)
        self.incoming_channel = _get_channel_name(incoming_channel)

        self.use_ssl = use_ssl

        self.redis_connection = Redis(host=self.redis_host,
                                      port=self.redis_port,
                                      ssl=self.use_ssl,
                                      decode_responses=True)

        self.redis_pubsub = None
        if self.incoming_channel is not None:
            self.redis_pubsub = self.redis_connection.pubsub()
            self.redis_pubsub.subscribe(self.incoming_channel)

    def _publish(self, message):
        if self.outgoing_channel is None:
            raise Exception(f'No outgoing redis channel set. Cannot publish!')

        if type(message) is not str:
            message = str(message)
        self.redis_connection.publish(self.outgoing_channel, message)

    def _get_next_incoming_message(self):
        return self._get_incoming_message(None)  # wait indefinitely

    def _get_incoming_message(self, timeout_s):
        if self.incoming_channel is None:
            raise Exception(f'No incoming redis channel set. Cannot get incoming message!')
        message_dict = self.redis_pubsub.get_message(timeout=timeout_s)
        if message_dict is not None:
            return message_dict['data']
        else:
            return None

    def _send_request(self, request, timeout_s=5):
        self._publish(request)
