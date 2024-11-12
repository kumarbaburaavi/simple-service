#!/usr/bin/env python3

import argparse
import logging
import sys
from os import path

from device_activity_trigger.redis.command_transmitter import CommandTransmitter

LOG_FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'

APP_NAME = "trigger_custom_message"

DEFAULT_REDIS_HOST = "127.0.0.1"
DEFAULT_REDIS_PORT = "6379"

APP_BASE_DIR = path.dirname(path.realpath(__file__))


def main(options):
    log = logging.getLogger(APP_NAME)
    log.info("Running %s with options: %s", APP_NAME, options)
    transmitter = CommandTransmitter(log, options.redis_host, options.redis_port, use_ssl=options.redis_use_ssl)

    event_content = None
    if options.event_content is not None and options.event_content_file is not None:
        log.error(f'Both parameters "event_content" and "event_content_file" provided. Use only one!')
        return
    elif options.event_content is not None:
        event_content = options.event_content
    elif options.event_content_file is not None:
        event_content = options.event_content_file.read()

    transmitter.send_command(event_content)


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description='Sends a message to activity log application, via Redis. '
                                                 'Content of the Redis message is defined either by a content file '
                                                 'or an input argument string.')

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
    parser.add_argument('--event_content',
                        type=str, nargs="+",
                        help=f'Content of command. Use either this argument or "--event_content_file"')
    parser.add_argument('--event_content_file', type=argparse.FileType('r'),
                        help=f'File with content of command to send to the controller.')
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
