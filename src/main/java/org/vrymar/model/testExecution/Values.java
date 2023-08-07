package org.vrymar.model.testExecution;

import org.vrymar.model.common.Project;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Values model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Values {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("key")
    private String key;

    @JsonProperty("project")
    private Project project;

    @JsonProperty("testCase")
    private TestCase testCase;

    @JsonProperty("environment")
    private Environment environment;

    @JsonProperty("jiraProjectVersion")
    private JiraProjectVersion jiraProjectVersion;

    @JsonProperty("testExecutionStatus")
    private TestExecutionStatus testExecutionStatus;

    @JsonProperty("actualEndDate")
    private String actualEndDate;

    @JsonProperty("estimatedTime")
    private Integer estimatedTime;

    @JsonProperty("executionTime")
    private Integer executionTime;

    @JsonProperty("executedById")
    private String executedById;

    @JsonProperty("assignedToId")
    private String assignedToId;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("automated")
    private Boolean automated;

    @JsonProperty("customFields")
    private Object customFields;

    @JsonProperty("links")
    private Links links;
}
