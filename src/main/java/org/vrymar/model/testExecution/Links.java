package org.vrymar.model.testExecution;

import org.vrymar.model.common.Issues;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Links model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Links {

    @JsonProperty("self")
    private String self;

    @JsonProperty("issues")
    private List<Issues> issues;
}
