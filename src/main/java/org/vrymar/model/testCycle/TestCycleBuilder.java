package org.vrymar.model.testCycle;

import lombok.Builder;
import java.util.List;

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
