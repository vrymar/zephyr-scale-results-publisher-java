package org.vrymar.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issues {

    @JsonProperty("self")
    private String self;

    @JsonProperty("issueId")
    private Integer issueId;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("target")
    private String target;

    @JsonProperty("type")
    private String type;
}
