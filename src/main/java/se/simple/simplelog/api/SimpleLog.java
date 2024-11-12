package se.simple.simplelog.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimpleLog {

    @NotEmpty
    private String device;

    @NotNull
    private LocalDateTime time;

    @NotEmpty
    private String logType;

    private String logTitle;
    private String logMessage;

}
