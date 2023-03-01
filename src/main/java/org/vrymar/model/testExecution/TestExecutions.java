package org.vrymar.model.testExecution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestExecutions {

    @JsonProperty("next")
    private String next;

    @JsonProperty("startAt")
    private Integer startAt;

    @JsonProperty("maxResults")
    private Integer maxResults;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("isLast")
    private Boolean isLast;

    @JsonProperty("values")
    private List<Values> values;
}
