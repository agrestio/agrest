package org.example.constraints;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bean Validation annotations test,
 * sits in a separate package to not clash with `org.example.entity` package registration for other tests
 */
public class NonAgP2 {

    @Size(min = 2, max = 10)
    public String getSized() {
        return null;
    }

    @Min(1)
    @Max(5)
    public Integer getRanged() {
        return null;
    }

    @Pattern(regexp = "^[a-z]+$")
    public String getPatterned() {
        return null;
    }
}
