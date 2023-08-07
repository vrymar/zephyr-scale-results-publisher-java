package org.vrymar.model.testCycle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * TestCycleResponse model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCycleResponse {

    @JsonProperty("testCycle")
    private TestCycle testCycle;
}
