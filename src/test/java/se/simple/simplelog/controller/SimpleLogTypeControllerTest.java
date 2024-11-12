package se.simple.simplelog.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import se.simple.simplelog.service.LogService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SimpleLogTypeController.class)
class SimpleLogTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogService logServiceMock;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenLocale_WhenGettingSimpleLogs_ShouldReturnLogs() throws Exception {
        when(logServiceMock.getLogTypes()).thenReturn(List.of("DEVICE_ASSIGNED", "DEVICE_DEASSIGNED"));

        MvcResult result = mockMvc.perform(get("/api/types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<String> types = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(types).hasSize(2);
    }

}
