package se.simple.simplelog.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import se.simple.simplelog.api.SimpleLog;
import se.simple.simplelog.service.LogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Simple logs")
@OpenAPIDefinition(info=@Info(title="API documentation for Simple log service"))
public class SimpleLogController {

    private final LogService logService;

    @Autowired
    public SimpleLogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(summary = "Endpoint for getting logs for multiple devices")
    @Parameter(in = ParameterIn.HEADER,  name = "Accept-Language", description = "Expected language. Defaults to English (EN) if not provided")
    @GetMapping(value = "")
    public List<SimpleLog> logs(
            @Parameter(in = ParameterIn.QUERY, description = "A list of one or more device ids")
            @RequestParam @NotEmpty List<String> devices,
            @Parameter(in = ParameterIn.QUERY, description = "Adds a filter for a 'from' datetime")
            @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(in = ParameterIn.QUERY, description = "Adds a filter for a 'to' datetime") @RequestParam(required = false) LocalDateTime toDate,
            @Parameter(in = ParameterIn.QUERY, description = "Adds a filter for number of days back in time starting from 'now'")
            @Positive(message = "Page number must be greater than 0") @RequestParam(required = false) Integer days,
            Locale locale) {
        if ((Objects.nonNull(fromDate) || Objects.nonNull(toDate)) && Objects.nonNull(days)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "parameter 'days' is not allowed in combination with date filer.");
        }

        if (Objects.isNull(fromDate) && Objects.isNull(toDate) && Objects.nonNull(days)) {
            fromDate = LocalDateTime.now().minusDays(days);
        }

        return logService.getLogs(devices, fromDate, toDate, locale);
    }

}
