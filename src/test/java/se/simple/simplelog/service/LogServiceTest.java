package se.simple.simplelog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.locale.LocaleHandler;
import se.simple.simplelog.model.Log;
import se.simple.simplelog.model.LogType;
import se.simple.simplelog.repository.LogRepository;
import se.simple.simplelog.repository.LogTypeRepository;
import se.simple.simplelog.util.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogRepository logRepositoryMock;

    @Mock
    private LogTypeRepository logTypeRepository;

    @Mock
    private LocaleHandler localeHandlerMock;

    @Test
    void givenSimpleLog_WhenStoring_ShouldStoreSimpleLog() {
        // Given
        Log log = new Log(1L, "test", LocalDateTime.now(), new LogType(1L, "TYPE"));
        when(logRepositoryMock.save(isA(Log.class))).thenReturn(log);
        when(logTypeRepository.findByName("TYPE")).thenReturn(Optional.of(new LogType(1L, "TYPE")));

        // When
        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        SimpleLog simpleLog = new SimpleLog("device", LocalDateTime.now(), "TYPE", "title", "message");
        logService.store(simpleLog);

        // Then
        verify(logRepositoryMock).save(isA(Log.class));
    }

    @Test
    void givenLogs_WhenGettingAllLogs_ShouldReturnAllLogs() {
         // Given
        Log log1 = new Log(1L, "test1", LocalDateTime.now(), new LogType(1L, "TYPE1"));
        Log log2 = new Log(2L, "test2", LocalDateTime.now(), new LogType(2L, "TYPE2"));
        when(logRepositoryMock.findAllByOrderByCreatedDesc()).thenReturn(List.of(log1, log2));

        // When
        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        List<SimpleLog> logs = logService.getLogs(Locale.ENGLISH);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(SimpleLog::getDevice).containsExactly("test1", "test2");
    }

    @Test
    void givenLogs_WhenGettingDeviceLogs_ShouldReturnDeviceLogs() {
        // Given
        Log log1 = new Log(1L, "test1", LocalDateTime.now(), new LogType(1L, "TYPE1"));
        Log log2 = new Log(2L, "test1", LocalDateTime.now(), new LogType(2L, "TYPE2"));
        when(logRepositoryMock.findByDeviceInOrderByCreatedDesc(List.of("test1"))).thenReturn(List.of(log1, log2));

        // When
        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        List<SimpleLog> logs = logService.getLogs(List.of("test1"), null, null, Locale.ENGLISH);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(SimpleLog::getDevice).containsOnly("test1");
    }

    @Test
    void givenLogs_WhenGettingDeviceLogsBetweenDates_ShouldReturnDeviceLogs() {
        // Given
        Log log1 = new Log(1L, "test1", LocalDateTime.now(), new LogType(1L, "TYPE1"));
        Log log2 = new Log(2L, "test1", LocalDateTime.now(), new LogType(2L, "TYPE2"));
        when(logRepositoryMock.findByDeviceInAndCreatedBetweenOrderByCreatedDesc(eq(List.of("test1")), isA(LocalDateTime.class), isA(LocalDateTime.class)))
                .thenReturn(List.of(log1, log2));

        // When
        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        List<SimpleLog> logs = logService.getLogs(List.of("test1"), LocalDateTime.now(), LocalDateTime.now(), Locale.ENGLISH);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(SimpleLog::getDevice).containsOnly("test1");
    }

    @Test
    void givenLogs_WhenGettingDeviceLogsFromDate_ShouldReturnDeviceLogs() {
        // Given
        Log log1 = new Log(1L, "test1", LocalDateTime.now(), new LogType(1L, "TYPE1"));
        Log log2 = new Log(2L, "test1", LocalDateTime.now(), new LogType(2L, "TYPE2"));
        when(logRepositoryMock.findByDeviceInAndCreatedGreaterThanOrderByCreatedDesc(eq(List.of("test1")), isA(LocalDateTime.class)))
                .thenReturn(List.of(log1, log2));

        // When
        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        List<SimpleLog> logs = logService.getLogs(List.of("test1"), LocalDateTime.now(), null, Locale.ENGLISH);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(SimpleLog::getDevice).containsOnly("test1");
    }

    @Test
    void givenLogs_WhenGettingDeviceLogsToDate_ShouldReturnDeviceLogs() {
        // Given
        Log log1 = new Log(1L, "test1", LocalDateTime.now(), new LogType(1L, "TYPE1"));
        Log log2 = new Log(2L, "test1", LocalDateTime.now(), new LogType(2L, "TYPE2"));
        when(logRepositoryMock.findByDeviceInAndCreatedLessThanOrderByCreatedDesc(eq(List.of("test1")), isA(LocalDateTime.class)))
                .thenReturn(List.of(log1, log2));

        // When
        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        List<SimpleLog> logs = logService.getLogs(List.of("test1"), null, LocalDateTime.now(), Locale.ENGLISH);

        // Then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(SimpleLog::getDevice).containsOnly("test1");
    }

    @Test
    void givenLogTypes_WhenGettingLogTypes_ShouldReturnLogTypes() {
        LogType logType = new LogType(1L, "test");
        when(logTypeRepository.findAll()).thenReturn(List.of(logType));

        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        List<String> logTypes = logService.getLogTypes();
        assertThat(logTypes).hasSize(1);
    }

    @Test
    void givenDeviceLog_WhenDelete_ShouldRemoveDeviceLogs() {
        // Give
        String DEVICE = "device1";

        // When
        LogService logService = new LogService(logRepositoryMock, logTypeRepository, new Mapper(localeHandlerMock));
        logService.delete(DEVICE);

        // Then
        verify(logRepositoryMock, times(1)).deleteByDevice(DEVICE);
    }

}
