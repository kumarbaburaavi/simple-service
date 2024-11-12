package se.simple.simplelog.locale;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleHandler {

    private final ResourceBundleMessageSource messageSource;

    public LocaleHandler(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String localize(String propertyPrefix, String property, Locale locale) {
        try {
            return messageSource.getMessage(propertyPrefix + "." + property.toLowerCase(), null, locale);
        } catch (NoSuchMessageException e) {
            return null;
        }
    }
}
