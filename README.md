# Simple service

A Spring Boot demo project for storing simple logs to a database. All logs are connected to a "device".      
The service listens for incoming events (Redis queue) and stores them. Each log entry is connected to a device and a log type.    
A REST API is provided to query for logs.

# Documentation
at `/swagger-ui.html`.

# API
at `/api/<resource>`. E.g. `http://localhost:5010/api/logs`

## Dependencies
The service connects to a Postgres DB and a Redis cluster.

## Local environment

1. Make sure you have Java 21 installed as this is the default version when building and when running the service (in docker).

2. Make sure Docker is installed locally.

3. Build locally with maven `mvn clean install`.

4. Run the backend service. This can be done in one of the following ways:
   1. Run directly from your IDE with the spring profile `local` enabled. 
      1. NOTE: The service depends on having both a database- and a Redis-connection. Therefore, both a Redis instance and a database needs to be running. This can be achieved by running these parts in Docker, with command `docker compose up simple-log-db simple-log-redis`
   2. Run the complete system from docker with `docker compose up --build`. The profile `docker` is automatically enabled.

5. Access API with `http://localhost:5010`

## Support scripts

### Redis message trigger
Under folder /scripts/redis_message_trigger a support script is available that can be used to publish Redis messages to the device-activity channel, which the simple-log-service subscribes to. This allows for easy testing of the system. Check the [README.md](scripts/redis_message_trigger/README.md) in the folder for more information.

