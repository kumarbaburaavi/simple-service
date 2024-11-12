#!/usr/bin/env python3

import sys
import argparse
import logging
from os import path

from device_activity_trigger.redis.command_transmitter import CommandTransmitter
from device_activity_trigger.redis.messages.delete import DeleteMessage
from device_activity_trigger.redis.messages.event import EventType, EventMessage
from device_activity_trigger.redis.messages.message import MessageType, RedisMessage

LOG_FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'

APP_NAME = "trigger_message"

DEFAULT_REDIS_HOST = "127.0.0.1"
DEFAULT_REDIS_PORT = "6379"

APP_BASE_DIR = path.dirname(path.realpath(__file__))


def main(options):
    log = logging.getLogger(APP_NAME)
    log.info("Running %s with options: %s", APP_NAME, options)
    transmitter = CommandTransmitter(log, options.redis_host, options.redis_port, use_ssl=options.redis_use_ssl)

    if options.message_type == MessageType.EVENT:
        message = EventMessage(options.event_type, options.device)
    elif options.message_type == MessageType.DELETE:
        message = DeleteMessage(options.device)
    else:
        message = RedisMessage(options.message_type)

    transmitter.send_command(message)


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description='Sends a message to activity log application, via Redis. ')

    parser.add_argument('--redis_host',
                        type=str, default=DEFAULT_REDIS_HOST,
                        help=f'Host for the Redis server which the application will communicate with. '
                             f'Default: {DEFAULT_REDIS_HOST}')
    parser.add_argument('--redis_port',
                        type=str, default=DEFAULT_REDIS_PORT,
                        help=f'Port for the Redis server which the application will communicate with. '
                             f'Default: {DEFAULT_REDIS_PORT}')
    parser.add_argument('--redis_use_ssl',
                        dest='redis_use_ssl', action='store_true',
                        help='If set, SSL connection will be used towards Redis server.')
    parser.add_argument('--message_type',
                        choices=list(MessageType), default=MessageType.EVENT,
                        type=MessageType,
                        help=f'Message type to send.')
    parser.add_argument('--event_type',
                        choices=list(EventType), default=EventType.DEVICE_ASSIGNED,
                        type=EventType,
                        help=f'Event type to send. Only valid if --message_type is set to {MessageType.EVENT}')
    parser.add_argument('--device',
                        type=str, default="TEST_DEVICE",
                        help=f'Device to send in event message. Only valid if --message_type is set to {MessageType.EVENT}')
    parser.add_argument('--log_file', metavar='l', type=argparse.FileType('w'),
                        help=f'Log output file.')
    parser.add_argument('--verbose',
                        dest='verbose', action='store_true',
                        help=f'If set, debug-level logs will be output.')

    args = parser.parse_args()

    log_level = logging.DEBUG if args.verbose else logging.INFO

    if args.log_file is None:
        logging.basicConfig(stream=sys.stdout, level=log_level, format=LOG_FORMAT)
    else:
        logging.basicConfig(filename=args.log_file, encoding='utf-8', level=log_level, format=LOG_FORMAT)
    logger = logging.getLogger(APP_NAME)

    main(args)
