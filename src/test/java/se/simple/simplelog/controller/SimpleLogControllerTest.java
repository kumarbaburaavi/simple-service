package se.simple.simplelog.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.service.LogService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SimpleLogController.class)
class SimpleLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogService logServiceMock;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<LocalDateTime> logServiceArgumentCaptor;

    @Test
    void givenDeviceAndLocale_WhenGettingSimpleLogs_ShouldReturnLogs() throws Exception {
        SimpleLog simpleLog1 = new SimpleLog("device1", LocalDateTime.now(), "type", "title", "message");
        SimpleLog simpleLog2 = new SimpleLog("device1", LocalDateTime.now(), "type", "title", "message");
        when(logServiceMock.getLogs(eq(List.of("device1")), isNull(), isNull(), isA(Locale.class))).thenReturn(List.of(simpleLog1, simpleLog2));

        MvcResult result = mockMvc.perform(get("/api/logs").queryParam("devices", "device1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<SimpleLog> logs = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(logs).hasSize(2);
        assertThat(logs).usingRecursiveAssertion().hasNoNullFields();
    }

    @Test
    void givenDevicesAndLocale_WhenGettingSimpleLogsForMultipleDevices_ShouldReturnLogs() throws Exception {
        SimpleLog simpleLog1 = new SimpleLog("device1", LocalDateTime.now(), "type", "title", "message");
        SimpleLog simpleLog2 = new SimpleLog("device2", LocalDateTime.now(), "type", "title", "message");
        when(logServiceMock.getLogs(eq(List.of("device1", "device2")), isNull(), isNull(), isA(Locale.class))).thenReturn(List.of(simpleLog1, simpleLog2));

        MvcResult result = mockMvc.perform(get("/api/logs").queryParam("devices", "device1", "device2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<SimpleLog> logs = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(logs).hasSize(2);
        assertThat(logs).usingRecursiveAssertion().hasNoNullFields();
    }

    @Test
    void givenDeviceAndLocale_WhenGettingSimpleLogsForDateInterval_ShouldReturnLogs() throws Exception {
        SimpleLog simpleLog1 = new SimpleLog("device1", LocalDateTime.now(), "type", "title", "message");
        SimpleLog simpleLog2 = new SimpleLog("device1", LocalDateTime.now(), "type", "title", "message");
        when(logServiceMock.getLogs(eq(List.of("device1")), isA(LocalDateTime.class), isA(LocalDateTime.class), isA(Locale.class)))
                .thenReturn(List.of(simpleLog1, simpleLog2));

        MvcResult result = mockMvc.perform(get("/api/logs").queryParam("devices", "device1")
                        .queryParam("fromDate", "2024-04-10T12:00")
                        .queryParam("toDate", "2024-04-11T06:00"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<SimpleLog> logs = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(logs).hasSize(2);
        verify(logServiceMock).getLogs(anyList(), logServiceArgumentCaptor.capture(), logServiceArgumentCaptor.capture(), isA(Locale.class));
        assertThat(logServiceArgumentCaptor.getAllValues()).hasSize(2);
        assertThat(logServiceArgumentCaptor.getAllValues().stream().toList())
                .containsExactly(
                        LocalDateTime.of(2024, 4, 10, 12, 0),
                        LocalDateTime.of(2024, 4, 11, 6, 0)
                );
    }

    @Test
    void givenDeviceAndLocale_WhenGettingSimpleLogsForDays_ShouldReturnLogs() throws Exception {
        SimpleLog simpleLog1 = new SimpleLog("device1", LocalDateTime.now(), "type", "title", "message");
        SimpleLog simpleLog2 = new SimpleLog("device1", LocalDateTime.now(), "type", "title", "message");
        when(logServiceMock.getLogs(eq(List.of("device1")), isA(LocalDateTime.class), isNull(), isA(Locale.class)))
                .thenReturn(List.of(simpleLog1, simpleLog2));

        LocalDateTime now = LocalDateTime.now().minusDays(3);
        MvcResult result = mockMvc.perform(get("/api/logs").queryParam("devices", "device1")
                        .queryParam("days", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<SimpleLog> logs = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(logs).hasSize(2);
        verify(logServiceMock).getLogs(anyList(), logServiceArgumentCaptor.capture(), logServiceArgumentCaptor.capture(), isA(Locale.class));
        assertThat(logServiceArgumentCaptor.getAllValues()).hasSize(2);
        assertThat(logServiceArgumentCaptor.getAllValues().get(0)).isCloseTo(now, within(1, ChronoUnit.MINUTES));
        assertThat(logServiceArgumentCaptor.getAllValues().get(1)).isNull();
    }

    @ParameterizedTest
    @MethodSource("parameterMethodSource")
    void givenDeviceAndLocale_WhenCallingWithWrongParameters_ShouldReturnBadRequest(String param, List<String> devices,
                                                                                    MultiValueMap<String, String> params) throws Exception {
        mockMvc.perform(get("/api/logs").queryParam(param, devices.toArray(new String[0]))
                        .params(params))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    private static Stream<? extends Arguments> parameterMethodSource() {
        MultiValueMap<String, String> params1 = new LinkedMultiValueMap<>();
        params1.add("days", "0");

        MultiValueMap<String, String> params2 = new LinkedMultiValueMap<>();
        params2.add("days", "1");
        params2.add("fromDate", "2024-01-01T12:00");
        params2.add("toDate", "2024-01-02T12:00");

        MultiValueMap<String, String> params3 = new LinkedMultiValueMap<>();
        params2.add("days", "1");

        return Stream.of(
                Arguments.of("devices", List.of("device1"), params1),
                Arguments.of("devices", List.of("device1"), params2),
                Arguments.of("devices", List.of(""), params3)
        );
    }


}
