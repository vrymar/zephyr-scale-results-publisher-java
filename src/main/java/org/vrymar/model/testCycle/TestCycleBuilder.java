package org.vrymar.model.testCycle;

import lombok.Builder;
import java.util.List;

/**
 * TestCycle object builder
 */
@Builder
public class TestCycleBuilder {

    private String name;
    private String description;
    private Integer jiraProjectVersion;
    private Integer folderId;
    private String customFields;


    @Override
    public String toString() {
        return String.format("{\"name\":\"%s\", \"description\":\"%s\", \"jiraProjectVersion\":%s, \"folderId\":%s, \"customFields\":%s}",
                name, description, jiraProjectVersion, folderId, customFields);
    }

    /**
     * Build customized test cycle
     * @param name  test cycle name
     * @param description  test cycle description
     * @param foldersIds test cycle folders ids
     * @param jiraProjectVersion  test cycle jira project version
     * @param customFields  test cycle custom fields
     * @return test cycle as a string
     */
    public static String generateTestCycle(String name, String description, List<Integer> foldersIds,
                                     int jiraProjectVersion, String customFields) {

        if (foldersIds.size() != 1) {
            return null;
        }

        return builder()
                .name(name)
                .description(description)
                .jiraProjectVersion(jiraProjectVersion)
                .folderId(foldersIds.get(0))
                .customFields(customFields)
                .build().toString();
    }
}
