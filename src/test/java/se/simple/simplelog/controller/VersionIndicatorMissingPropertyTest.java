package se.simple.simplelog.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = VersionIndicator.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class VersionIndicatorMissingPropertyTest {

    @Test
    void givenNoApplicationVersion_WhenGettingVersion_ShouldGetUnknown(@Autowired VersionIndicator versionIndicator) {
        assertThat(versionIndicator.health().getDetails().get("version")).hasToString("unknown");
    }

}
