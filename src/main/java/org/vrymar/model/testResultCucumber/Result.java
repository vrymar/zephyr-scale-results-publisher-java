package org.vrymar.model.testResultCucumber;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Result model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    @JsonProperty("status")
    private String status;
}
