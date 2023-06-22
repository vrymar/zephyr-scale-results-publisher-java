package org.vrymar.utils;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesUtil {
    private final Path propertiesFilePath;
    private final Properties properties;
    @Getter
    private final String baseUri;
    @Getter
    private final String resultsFolder;
    @Getter
    private final String uriSuffix;
    @Getter
    private String zephyrToken;
    @Getter
    private final String projectKey;
    @Getter
    private final String autoCreateTestCases;
    @Getter
    private final String resultsFileExtension;
    @Getter
    private final boolean customTestCycle;
    @Getter
    private final String testCycleName;
    @Getter
    private final String testCycleFolderName;
    @Getter
    private final String testCycleDescription;
    @Getter
    private final Integer testCycleJiraProjectVersion;
    @Getter
    private final String customFields;
    @Getter
    private final String folderType;
    @Getter
    private final String maxResults;
    @Getter
    private final boolean updateTestCasesWithLabels;
    @Getter
    private final boolean updateTestCasesWithStories;
    @Getter
    private String jiraBaseUri;
    @Getter
    private String jiraToken;
    @Getter
    String jiraUserEmail;

    @SneakyThrows
    public PropertiesUtil(Path propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
        properties = new Properties();
        baseUri = readProperties("baseUri");
        uriSuffix = readProperties("uriSuffix");
        zephyrToken = System.getenv("ZEPHYR_TOKEN");
        if (zephyrToken == null) {
            zephyrToken = readProperties("zephyrToken");
        }
        projectKey = readProperties("projectKey");
        autoCreateTestCases = readProperties("autoCreateTestCases");
        resultsFileExtension = readProperties("resultsFileExtension");
        resultsFolder = readProperties("resultsFolder");
        customTestCycle = Boolean.parseBoolean(readProperties("customTestCycle"));
        testCycleName = readProperties("testCycleName");
        testCycleFolderName = readProperties("testCycleFolderName");
        testCycleDescription = readProperties("testCycleDescription");
        testCycleJiraProjectVersion = Integer.parseInt(readProperties("testCycleJiraProjectVersion"));
        customFields = readProperties("customFields");
        folderType = readProperties("folderType");
        maxResults = readProperties("maxResults");
        updateTestCasesWithLabels = Boolean.parseBoolean(readProperties("updateTestCasesWithLabels"));
        updateTestCasesWithStories = Boolean.parseBoolean(readProperties("updateTestCasesWithStories"));
        jiraBaseUri = readProperties("jiraBaseUri");
        if (jiraBaseUri == null) {
            jiraBaseUri = readProperties("jiraBaseUri");
        }
        jiraToken = System.getenv("JIRA_TOKEN");
        if (jiraToken == null) {
            jiraToken = readProperties("jiraToken");
        }
        jiraUserEmail = System.getenv("JIRA_USER_EMAIL");
        if (jiraUserEmail == null) {
            jiraUserEmail = readProperties("jiraUserEmail");
        }
    }

    public String readProperties(String key) throws IOException {
        try (InputStream is = new FileInputStream(propertiesFilePath.toString())) {
            properties.load(is);
        }
        return properties.getProperty(key);
    }
}
