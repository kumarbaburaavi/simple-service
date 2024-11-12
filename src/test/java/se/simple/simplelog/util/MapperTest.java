package se.simple.simplelog.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.locale.LocaleHandler;
import se.simple.simplelog.model.Log;
import se.simple.simplelog.model.LogType;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapperTest {

    @Mock
    private LocaleHandler localeHandlerMock;

    @Test
    void givenLog_WhenMapping_ShouldMapToSimpleLog() {
        Mapper mapper = new Mapper(localeHandlerMock);
        Log log = new Log(1L, "device", LocalDateTime.now(), new LogType(1L, "type"));
        Locale locale = Locale.ENGLISH;
        when(localeHandlerMock.localize("message", "type", locale)).thenReturn("aMessage");
        when(localeHandlerMock.localize("title", "type", locale)).thenReturn("aTitle");

        SimpleLog simpleLog = mapper.map(log, locale);
        assertThat(simpleLog).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(simpleLog.getDevice()).isEqualTo(log.getDevice());
        assertThat(simpleLog.getTime()).isEqualTo(log.getCreated());
        assertThat(simpleLog.getLogType()).isEqualTo(log.getType().getName());
        assertThat(simpleLog.getLogMessage()).isEqualTo("aMessage");
        assertThat(simpleLog.getLogTitle()).isEqualTo("aTitle");
    }

    @Test
    void givenSimpleLog_WhenMapping_ShouldMapToLog() {
        SimpleLog simpleLog = new SimpleLog("device", LocalDateTime.now(), "type", "title", "message");
        Mapper mapper = new Mapper(localeHandlerMock);
        LogType logType = new LogType(1L, "type");
        Log log = mapper.map(simpleLog, logType);
        assertThat(log).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
        assertThat(log.getDevice()).isEqualTo(simpleLog.getDevice());
        assertThat(log.getCreated()).isEqualTo(simpleLog.getTime());
        assertThat(log.getType().getName()).isEqualTo(simpleLog.getLogType());
    }

    @Test
    void givenLogType_WhenMapping_ShouldMapToString() {
        LogType logType = new LogType(1L, "type");
        Mapper mapper = new Mapper(localeHandlerMock);
        assertThat(mapper.map(logType)).isEqualTo("type");
    }

}
