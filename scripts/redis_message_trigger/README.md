# Redis message triggering scripts

# Requirements
* Python
   * Developed with python3.10. Compatibility below this version is untested.
* A running Redis cache which the script can publish events to.

# Quick start guide
1. `python3 -m venv .venv`
2. `source .venv/bin/activate`
3. `python3 -m pip install -r requirements.txt`
4. `python3 trigger_message.py`

# trigger_message.py
The quick guide above will trigger a default event of type 'DEVICE_ASSIGNED' for device 'TEST_DEVICE' to a Redis cache running on host 127.0.0.1 with port 6379. All of these settings can however be modified with input arguments. To see a complete description of the available input arguments, run `python3 trigger_message.py --help`.

The time of the events triggered by this script will be set to the current time, at the time of execution.

Below are a few examples on how to trigger different types of messages.

## Event messages
### Triggering specific types of events
* `python3 trigger_message.py --message_type EVENT --event_type DEVICE_ASSIGNED`

### Triggering events for different devices
* `python3 trigger_message.py --device my_device`
* `python3 trigger_message.py --message_type EVENT  --device ANOTHER_TEST_DEVICE --event_type DEVICE_ASSIGNED`

## Delete messages
* `python3 trigger_message.py --message_type DELETE --device my_device`
* `python3 trigger_message.py --message_type DELETE --device ANOTHER_TEST_DEVICE`


# trigger_custom_message.py
As an alternative to the trigger_message.py script, described above, the script `trigger_custom_message.py` also exists. The functionality is similar, but here the contents of of the Redis message is completely customizable by defining an explicit JSON either via a content file (using argument `--event_content_file`) or via command line (using argument `--event_content`).

An example JSON-file to use for publishing an event with this script is available at /cfg/event_example.json. The following is an example on how to run with this example JSON:
`python3 trigger_custom_event.py --event_content_file cfg/event_example.json`
