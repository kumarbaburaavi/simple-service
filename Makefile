all: clean build docker-start

clean:
	mvn clean

build:
	mvn install

docker-start:
	docker compose up --build -d

trigger-message-script: prepare-python-script run-python-script-trigger-message-device-assigned

prepare-python-script:
	cd scripts/redis_message_trigger; python3 -m venv .venv; . .venv/bin/activate; python3 -m pip install -r requirements.txt

run-python-script-trigger-message-device-assigned:
	cd scripts/redis_message_trigger; . .venv/bin/activate; python3 trigger_message.py --event_type=DEVICE_ASSIGNED
