package se.simple.simplelog.locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class LocaleHandlerTest {

    @Autowired
    private LocaleHandler localeHandler;

    @ParameterizedTest
    @MethodSource("localeMethodSource")
    void givenProperties_WhenLocalizing_ShouldReturnLocalizedProperty(String property, Locale locale, String expectedTitle, String expectedMessage) {
        assertThat(localeHandler.localize("title", property, locale)).isEqualTo(expectedTitle);
        assertThat(localeHandler.localize("message", property, locale)).isEqualTo(expectedMessage);
    }

    @Test
    void givenUnknownProperty_WhenLocalizing_ShouldReturnNullMessage() {
        assertThat(localeHandler.localize("message", "unknown", Locale.ENGLISH)).isNull();
    }

    private static Stream<? extends Arguments> localeMethodSource() {
        return Stream.of(
                Arguments.of("device_assigned", Locale.ENGLISH, "Device assigned", "The device has been assigned to a user"),
                Arguments.of("device_assigned", Locale.of("SV"), "Enheten tilldelad", "Enheten har tilldelats en användare"),
                Arguments.of("device_deassigned", Locale.ENGLISH, "Device de-assigned", "The device has been de-assigned to a user"),
                Arguments.of("device_deassigned", Locale.of("SV"), "Enheten av tilldelad", "Enheten har av tilldelats en användare")
        );
    }

}
