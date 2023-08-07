package org.vrymar.model.testCycle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * TestCycle model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCycle {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("url")
    private String url;

    @JsonProperty("key")
    private String key;
}
