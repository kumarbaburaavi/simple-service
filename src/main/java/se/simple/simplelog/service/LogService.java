package se.simple.simplelog.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.exception.LogTypeNotFoundException;
import se.simple.simplelog.model.Log;
import se.simple.simplelog.model.LogType;
import se.simple.simplelog.repository.LogRepository;
import se.simple.simplelog.repository.LogTypeRepository;
import se.simple.simplelog.util.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final LogRepository logRepository;
    private final LogTypeRepository logTypeRepository;
    private final Mapper mapper;

    @Autowired
    public LogService(LogRepository logRepository, LogTypeRepository logTypeRepository, Mapper mapper) {
        this.logRepository = logRepository;
        this.logTypeRepository = logTypeRepository;
        this.mapper = mapper;
    }

    @Async
    @Transactional
    public CompletableFuture<SimpleLog> store(@Valid SimpleLog simpleLog) {
        try {
            return executeStore(simpleLog);
        } catch (Exception e) {
            logger.warn("Failed to store simple log.", e);
            return null;
        }
    }

    @Async
    @Transactional
    public void delete(@NotNull String device) {
        try {
            executeDelete(device);
        } catch (Exception e) {
            logger.warn("Failed to store simple log.", e);
        }
    }

    public List<SimpleLog> getLogs(Locale locale) {
        return getLogs(logRepository.findAllByOrderByCreatedDesc(), locale);
    }

    public List<SimpleLog> getLogs(List<String> devices, LocalDateTime fromDate, LocalDateTime toDate, Locale locale) {
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate)) {
            return getLogs(logRepository.findByDeviceInAndCreatedBetweenOrderByCreatedDesc(devices, fromDate, toDate), locale);
        } else if (Objects.nonNull(fromDate)) {
            return getLogs(logRepository.findByDeviceInAndCreatedGreaterThanOrderByCreatedDesc(devices, fromDate), locale);
        } else if (Objects.nonNull(toDate)) {
            return getLogs(logRepository.findByDeviceInAndCreatedLessThanOrderByCreatedDesc(devices, toDate), locale);
        } else  {
            return getLogs(logRepository.findByDeviceInOrderByCreatedDesc(devices), locale);
        }
    }

    public List<String> getLogTypes() {
        return logTypeRepository.findAll().stream().map(mapper::map).toList();
    }

    protected CompletableFuture<SimpleLog> executeStore(SimpleLog simpleLog) {
        LogType logType = logTypeRepository.findByName(simpleLog.getLogType())
                .orElseThrow(() -> new LogTypeNotFoundException("Logtype rejected. Type '" + simpleLog.getLogType() + "' is not a known logtype."));
        return CompletableFuture.completedFuture(mapper.map(logRepository.save(mapper.map(simpleLog, logType)), null));
    }

    protected void executeDelete(String device) {
        logRepository.deleteByDevice(device);
    }

    private List<SimpleLog> getLogs(List<Log> logs, Locale locale) {
        return logs.stream().map(log -> mapper.map(log, locale)).toList();
    }
}
