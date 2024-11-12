package se.simple.simplelog.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = VersionIndicator.class)
@SetEnvironmentVariable(key = "application.version", value = "1.0")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class VersionIndicatorPropertyTest {

    @Test
    void givenApplicationVersion_WhenGettingVersion_ShouldGetVersion(@Autowired VersionIndicator versionIndicator) {
        assertThat(versionIndicator.health().getDetails().get("version")).hasToString("1.0");
    }

    @ParameterizedTest
    @CsvSource(value = {"1.0,1.0", "null,unknown", "'',unknown"}, nullValues = "null")
    void test(String version, String expectedVersion) {
        VersionIndicator versionIndicator = new VersionIndicator(version);
        assertThat(versionIndicator.health().getDetails().get("version")).hasToString(expectedVersion);
    }

}
