package se.simple.simplelog.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.simple.simplelog.model.Log;
import se.simple.simplelog.model.LogType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DataJpaTest
@ActiveProfiles("integrationtest")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional(propagation = Propagation.NEVER)
@Rollback(value = false)
class LogRepositoryIntegrationTest {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogTypeRepository logTypeRepository;

    @AfterEach
    public void cleanupAfterEach() {
        logRepository.deleteAll();
    }

    private LogType getLogType(String name) {
        Optional<LogType> logType = logTypeRepository.findByName(name);
        if (logType.isEmpty()) {
            fail("LogType " + name + " not found in DB");
        } else {
            return logType.get();
        }
        return null;
    }

    @Test
    void givenLogs_WhenGettingLogs_ShouldReturnLogs() {
        // Assert initial state
        assertThat(logRepository.findAll()).isEmpty();
        LogType deviceAssignedType = getLogType("DEVICE_ASSIGNED");
        assertThat(deviceAssignedType).isNotNull();

        // Create logs
        Log log1 = new Log(null, "device1", LocalDateTime.now().minusHours(4), deviceAssignedType);
        Log log2 = new Log(null, "device1", LocalDateTime.now().minusHours(3), deviceAssignedType);
        Log log3 = new Log(null, "device1", LocalDateTime.now().minusHours(2), deviceAssignedType);
        Log log4 = new Log(null, "device1", LocalDateTime.now().minusHours(1), deviceAssignedType);
        Log log5 = new Log(null, "device2", LocalDateTime.now().minusHours(1), deviceAssignedType);
        Log log6 = new Log(null, "device2", LocalDateTime.now(), deviceAssignedType);
        Log log7 = new Log(null, "device3", LocalDateTime.now(), deviceAssignedType);
        Log log8 = new Log(null, "device1", LocalDateTime.now(), deviceAssignedType);
        Log log9 = new Log(null, "device3", LocalDateTime.now(), deviceAssignedType);
        List<Log> logs = List.of(log1, log2, log3, log4, log5, log6, log7, log8, log9);
        logRepository.saveAll(logs);

        // Assert getting all logs
        List<Log> savedLogs = logRepository.findAllByOrderByCreatedDesc();
        assertThat(savedLogs).hasSize(9).extracting(Log::getDevice)
                .containsOnly("device1", "device2", "device3");

        // Assert getting device logs
        savedLogs = logRepository.findByDeviceInOrderByCreatedDesc(List.of("device1"));
        assertThat(savedLogs).hasSize(5).extracting(Log::getDevice)
                .containsOnly("device1");

        // Assert getting multiple device logs
        savedLogs = logRepository.findByDeviceInOrderByCreatedDesc(List.of("device1", "device3"));
        assertThat(savedLogs).hasSize(7).extracting(Log::getDevice)
                .containsOnly("device1", "device3");

        // Assert getting device logs between dates
        savedLogs = logRepository.findByDeviceInAndCreatedBetweenOrderByCreatedDesc(List.of("device1"),
                LocalDateTime.now().minusHours(3).minusMinutes(20), LocalDateTime.now().minusHours(2).plusMinutes(10));
        assertThat(savedLogs).hasSize(2).extracting(Log::getDevice)
                .containsOnly("device1");
        assertThat(savedLogs).extracting(Log::getType).extracting(LogType::getName)
                .containsOnly(deviceAssignedType.getName());

        // Assert getting device logs greater then
        savedLogs = logRepository.findByDeviceInAndCreatedGreaterThanOrderByCreatedDesc(List.of("device1"),
                LocalDateTime.now().minusHours(1).minusMinutes(10));
        assertThat(savedLogs).hasSize(2).extracting(Log::getDevice)
                .containsOnly("device1");
        assertThat(savedLogs).extracting(Log::getType).extracting(LogType::getName)
                .containsOnly(deviceAssignedType.getName(), deviceAssignedType.getName());

        // Assert getting device logs less then
        savedLogs = logRepository.findByDeviceInAndCreatedLessThanOrderByCreatedDesc(List.of("device2"),
                LocalDateTime.now().minusMinutes(10));
        assertThat(savedLogs).hasSize(1).extracting(Log::getDevice)
                .containsOnly("device2");
        assertThat(savedLogs).extracting(Log::getType).extracting(LogType::getName)
                .containsOnly(deviceAssignedType.getName());
    }

    @Transactional
    @Test
    void givenLogs_WhenDeletingLogs_ShouldRemoveLogsForDevice() {
        // Assert initial state
        assertThat(logRepository.findAll()).isEmpty();
        LogType deviceAssignedType = getLogType("DEVICE_ASSIGNED");
        assertThat(deviceAssignedType).isNotNull();

        // Create logs
        Log log1 = new Log(null, "device1", LocalDateTime.now().minusHours(4), deviceAssignedType);
        Log log2 = new Log(null, "device1", LocalDateTime.now().minusHours(3), deviceAssignedType);
        Log log3 = new Log(null, "device2", LocalDateTime.now().minusHours(2), deviceAssignedType);
        Log log4 = new Log(null, "device2", LocalDateTime.now().minusHours(1), deviceAssignedType);
        Log log5 = new Log(null, "device3", LocalDateTime.now().minusHours(1), deviceAssignedType);
        Log log6 = new Log(null, "device3", LocalDateTime.now(), deviceAssignedType);
        Log log7 = new Log(null, "device2", LocalDateTime.now(), deviceAssignedType);
        List<Log> logs = List.of(log1, log2, log3, log4, log5, log6, log7);
        logRepository.saveAll(logs);

        // assert all logs in DB before deletions
        assertThat(logRepository.findAllByOrderByCreatedDesc()).hasSize(7);

        // delete everything for device2
        logRepository.deleteByDevice("device2");

        // Assert device2 entries are removed
        List<Log> savedLogs = logRepository.findAllByOrderByCreatedDesc();
        assertThat(savedLogs).hasSize(4).extracting(Log::getDevice)
                .containsOnly("device1", "device3");
        assertThat(savedLogs).extracting(Log::getType).extracting(LogType::getName)
                .containsOnly(deviceAssignedType.getName(), deviceAssignedType.getName());
        savedLogs = logRepository.findByDeviceInOrderByCreatedDesc(List.of("device2"));
        assertThat(savedLogs).hasSize(0);

        // delete everything for device3
        logRepository.deleteByDevice("device3");

        // Assert device3 entries are removed
        savedLogs = logRepository.findAllByOrderByCreatedDesc();
        assertThat(savedLogs).hasSize(2).extracting(Log::getDevice)
                .containsOnly("device1");
        savedLogs = logRepository.findByDeviceInOrderByCreatedDesc(List.of("device3"));
        assertThat(savedLogs).hasSize(0);
    }

}
