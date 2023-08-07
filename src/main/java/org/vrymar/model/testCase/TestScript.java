package org.vrymar.model.testCase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * TestScript model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestScript {
    @JsonProperty("self")
    private String self;
}
