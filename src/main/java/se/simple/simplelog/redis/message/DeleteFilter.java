package se.simple.simplelog.redis.message;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeleteFilter {

    @NotEmpty
    private String device;

    @Override
    public String toString() {
        return device;
    }
}
