package se.simple.simplelog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("versionInfo")
public class VersionIndicator implements HealthIndicator {

    private final String version;
    private static final String DEFAULT_VERSION = "unknown";

    public VersionIndicator(@Value("${application.version:" + DEFAULT_VERSION + "}") String version) {
        this.version = version;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        healthBuilder.up().withDetail("version", ObjectUtils.isEmpty(version) ? DEFAULT_VERSION : version);
        return healthBuilder.build();
    }
}
