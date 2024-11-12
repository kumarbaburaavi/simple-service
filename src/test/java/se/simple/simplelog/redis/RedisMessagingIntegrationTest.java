package se.simple.simplelog.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.service.LogService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TestRedisConfiguration.class)
@ActiveProfiles("integrationtest")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional(propagation = Propagation.NEVER)
@Rollback(value = false)
class RedisMessagingIntegrationTest {

    @Autowired
    LogService logService;

    @Autowired
    RedisConfiguration redisConfiguration;

    @Test
    void givenEventMessage_WhenPublishing_ShouldReceiveAndStoreSimpleLog() throws JsonProcessingException {
        // Given
        String DEVICE = "MyTestDevice";
        String LOG_TYPE = "DEVICE_ASSIGNED";
        LocalDateTime ACTIVITY_TIME = LocalDateTime.now();

        String CHANNEL_NAME = "device_activity";

        String MESSAGE = generateEventJsonMessage(DEVICE, ACTIVITY_TIME, LOG_TYPE);
        Assertions.assertTrue(logService.getLogs(List.of(DEVICE), null, null, Locale.ENGLISH).isEmpty());

        // When
        try (Jedis jedis = new Jedis(redisConfiguration.getHostName(), redisConfiguration.getPort())) {
            jedis.publish(CHANNEL_NAME, MESSAGE);
        }
        await().atMost(1, TimeUnit.SECONDS).until(() -> !logService.getLogs(List.of(DEVICE), null, null, Locale.ENGLISH).isEmpty());

        // Then
        List<SimpleLog> deviceSimpleLogs = logService.getLogs(List.of(DEVICE), null, null, Locale.ENGLISH);
        Assertions.assertEquals(1, deviceSimpleLogs.size());
        SimpleLog deviceSimpleLog = deviceSimpleLogs.getFirst();
        Assertions.assertEquals(DEVICE, deviceSimpleLog.getDevice());
        Assertions.assertEquals(ACTIVITY_TIME.truncatedTo(ChronoUnit.MILLIS), deviceSimpleLog.getTime().truncatedTo(ChronoUnit.MILLIS));
        Assertions.assertEquals(LOG_TYPE, deviceSimpleLog.getLogType());
    }

    @Test
    void givenDeleteMessage_WhenPublishing_ShouldReceiveAndDeleteEntriesForDevice() throws JsonProcessingException {
        // Given
        String DEVICE1 = "MyTestDevice";
        String DEVICE2 = "AnotherTestDevice";
        String CHANNEL_NAME = "device_activity";

        // assert empty initial state
        Assertions.assertTrue(logService.getLogs(Locale.ENGLISH).isEmpty());

        SimpleLog simpleLog = new SimpleLog(DEVICE1, LocalDateTime.now(), "DEVICE_ASSIGNED", null, null);
        logService.store(simpleLog);
        SimpleLog simpleLog2 = new SimpleLog(DEVICE2, LocalDateTime.now(), "DEVICE_ASSIGNED", null, null);
        logService.store(simpleLog2);

        await().atMost(1, TimeUnit.SECONDS).until(() -> logService.getLogs(Locale.ENGLISH).size() == 2);

        // When
        String MESSAGE = generateDeleteJsonMessage(DEVICE1);
        try (Jedis jedis = new Jedis(redisConfiguration.getHostName(), redisConfiguration.getPort())) {
            jedis.publish(CHANNEL_NAME, MESSAGE);
        }
        await().atMost(1, TimeUnit.SECONDS).until(() -> logService.getLogs(List.of(DEVICE1), null, null, Locale.ENGLISH).isEmpty());

        // Then
        List<SimpleLog> device2SimpleLogs = logService.getLogs(List.of(DEVICE2), null, null, Locale.ENGLISH);
        Assertions.assertEquals(1, device2SimpleLogs.size());
        SimpleLog device2SimpleLog = device2SimpleLogs.getFirst();
        Assertions.assertEquals(DEVICE2, device2SimpleLog.getDevice());
    }

    private String generateEventJsonMessage(String device, LocalDateTime time, String logType) throws JsonProcessingException {
        Map<String,String> eventMap = new HashMap<>();
        eventMap.put("device", device);
        eventMap.put("time", time.toString());
        eventMap.put("logType", logType);

        Map<String,Object> messageMap = new HashMap<>();
        messageMap.put("messageType", "EVENT");
        messageMap.put("event", eventMap);

        return new ObjectMapper().writeValueAsString(messageMap);
    }

    private String generateDeleteJsonMessage(String device) throws JsonProcessingException {
        Map<String,String> filterMap = new HashMap<>();
        filterMap.put("device", device);

        Map<String,Object> messageMap = new HashMap<>();
        messageMap.put("messageType", "DELETE");
        messageMap.put("filter", filterMap);

        return new ObjectMapper().writeValueAsString(messageMap);
    }
}
