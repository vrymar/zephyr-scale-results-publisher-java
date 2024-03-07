package org.vrymar.model.testResultCucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Steps model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {
    
    @JsonProperty("result")
    private Result result;

    @JsonProperty("name")
    private String name;

    @JsonProperty("keyword")
    private String keyword;
}
