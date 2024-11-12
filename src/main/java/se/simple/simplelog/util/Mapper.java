package se.simple.simplelog.util;

import org.springframework.stereotype.Component;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.locale.LocaleHandler;
import se.simple.simplelog.model.Log;
import se.simple.simplelog.model.LogType;

import java.util.Locale;

@Component
public class Mapper {

    private final LocaleHandler localeHandler;

    public Mapper(LocaleHandler localeHandler) {
        this.localeHandler = localeHandler;
    }

    public Log map(SimpleLog simpleLog, LogType logType) {
        return new Log(null, simpleLog.getDevice(), simpleLog.getTime(), logType);
    }

    public SimpleLog map(Log log, Locale locale) {
        String localizedTitle = localeHandler.localize("title", log.getType().getName(), locale);
        String localizedMessage = localeHandler.localize("message", log.getType().getName(), locale);
        return new SimpleLog(log.getDevice(), log.getCreated(), log.getType().getName(), localizedTitle, localizedMessage);
    }

    public String map(LogType logType) {
        return logType.getName();
    }
}
