package org.vrymar.model.testCase;

import org.vrymar.model.common.Project;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Values {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("key")
    private String key;

    @JsonProperty("name")
    private String name;

    @JsonProperty("project")
    private Project project;

    @JsonProperty("createdOn")
    private String createdOn;

    @JsonProperty("objective")
    private String objective;

    @JsonProperty("precondition")
    private String precondition;

    @JsonProperty("estimatedTime")
    private Integer estimatedTime;

    @JsonProperty("labels")
    private List<String> labels;

    @JsonProperty("component")
    private Component component;

    @JsonProperty("priority")
    private Priority priority;

    @JsonProperty("status")
    private Status status;

    @JsonProperty("folder")
    private Folder folder;

    @JsonProperty("owner")
    private Owner owner;

    @JsonProperty("testScript")
    private TestScript testScript;

    @JsonProperty("customFields")
    private Object customFields;

    @JsonProperty("links")
    private Links links;
}
