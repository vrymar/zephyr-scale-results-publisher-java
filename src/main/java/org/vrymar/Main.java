package org.vrymar;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.vrymar.jiraClient.JiraIssuesClient;
import org.vrymar.model.jiraIssue.JiraIssue;
import org.vrymar.zephyrClient.TestCycleClient;
import org.vrymar.zephyrClient.TestCasesClient;
import org.vrymar.zephyrClient.TestExecutionsClient;
import org.vrymar.model.testCase.TestCase;
import org.vrymar.model.testExecution.TestExecutions;
import org.vrymar.model.testCycle.TestCycleResponse;
import org.vrymar.utils.FileUtil;
import org.vrymar.utils.PropertiesUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main execution class
 */
public class Main {

    private static final String PROP_FILE = "zephyr.properties";

    /**
     * Execute all actions
     *
     * @param args array of arguments
     */
    public static void main(String[] args) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        TestCycleClient publisher = new TestCycleClient(httpClient);
        FileUtil fileUtil = new FileUtil();
        TestExecutionsClient testExecutionsClient = new TestExecutionsClient(httpClient);
        TestCasesClient testCasesClient = new TestCasesClient(httpClient);
        JiraIssuesClient jiraIssuesClient = new JiraIssuesClient(httpClient);

        try {
            File resourceFile = fileUtil.findFile(PROP_FILE);
            System.out.println("Zephyr publisher: Resource file location: " + resourceFile.getAbsolutePath());
            PropertiesUtil propertiesUtil = new PropertiesUtil(resourceFile.toPath());
            String zephyrToken = propertiesUtil.getZephyrToken();

            if (zephyrToken == null || zephyrToken.isEmpty()) {
                System.out.println("Zephyr publisher: No Zephyr API Token is found! Test results will not be published.");
                return;
            }

            String resultsFileExtension = propertiesUtil.getResultsFileExtension();
            String resultsFolder = propertiesUtil.getResultsFolder();
            boolean isUpdateTestCasesWithLabels = propertiesUtil.isUpdateTestCasesWithLabels();
            boolean isUpdateTestCasesWithStories = propertiesUtil.isUpdateTestCasesWithStories();

            File file = fileUtil.findFile(resultsFolder);
            File resultsFile = new File(file.getAbsolutePath() + "/testResults.zip");
            fileUtil.deleteExistingFile(resultsFile);

            System.out.println("Zephyr publisher: Expected results file: " + resultsFile);
            Path folderPath = Paths.get(resultsFile.getParent());
            System.out.println("Zephyr publisher: Folder path to create zip file: " + folderPath);
            List<String> filePaths = fileUtil.findAllFilesWithExtension(folderPath, resultsFileExtension);
            File zipFile = fileUtil.createZip(resultsFile, filePaths);

            // Publish Test Cycle
            TestCycleResponse testCycleResponse = publisher.postTestCycle(propertiesUtil, zipFile);
            if (testCycleResponse == null || testCycleResponse.getTestCycle() == null) {
                System.out.println("Zephyr publisher: Error: Test cycle response is NULL or fails to be parsed. " +
                        "Please, check if the results are published as a new Test Cycle in Zephyr scale.");
                return;
            }

            String testCycleKey = testCycleResponse.getTestCycle().getKey();
            TestExecutions testExecutions = testExecutionsClient.getTestExecutions(propertiesUtil, testCycleKey);
            List<String> scenariosKeys = testExecutionsClient.getScenariosKeys(testExecutions);

            List<TestCase> testCases = new ArrayList<>();
            scenariosKeys.forEach(key -> {
                try {
                    TestCase testCase = testCasesClient.getTestCase(propertiesUtil, key);
                    testCases.add(testCase);

                } catch (URISyntaxException | IOException e) {
                    throw new RuntimeException(e);
                }
            });

            // Update Test Cases with script with steps
            testCases.forEach(testCase -> {
                Map<String, String> testScriptWithSteps;

                try {
                    testScriptWithSteps = fileUtil.getTestScenarioKeyAndStepsFromResultsFile(propertiesUtil, testCase);
                    testScriptWithSteps.forEach((key, value) -> {
                        try {
                            Integer responseCode = testCasesClient.createTestScriptWithSteps(propertiesUtil, key, "bdd", value);
                            System.out.println("Zephyr publisher: Create test script with steps in Test Case " + key + " response status code: " + responseCode);
                        } catch (URISyntaxException | IOException e) {
                            System.out.println("Zephyr publisher: Error: Failed to update test case with script with steps. Error: " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    System.out.println("Zephyr publisher: Error: Failed to update test case with script with steps. Error: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            // Update Test Cases with Labels
            if (isUpdateTestCasesWithLabels) {
                Map<String, List<String>> scenarioNameTagsLabels = fileUtil.getTestScenarioNameAndTagsFromResultsFile(propertiesUtil, "@ZephyrLabel");
                if (!scenarioNameTagsLabels.isEmpty()) {
                    scenarioNameTagsLabels.forEach((name, tagsLabels) -> testCases.forEach(testCase -> {
                        if (name.equals(testCase.getName())) {
                            TestCase newTestCase = testCasesClient.buildTestCaseBodyWithLabels(testCase, tagsLabels);
                            try {
                                testCasesClient.updateTestCase(propertiesUtil, testCase.getKey(), newTestCase);
                            } catch (URISyntaxException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }));
                } else {
                    System.out.println("Zephyr publisher: No tags found to add to Zephyr Scale test cases labels.");
                }
            }

            // Update Test Cases with Issues/Stories
            if (isUpdateTestCasesWithStories) {
                Map<String, List<String>> testScenarioNameTagsIssues = fileUtil.getTestScenarioNameAndTagsFromResultsFile(propertiesUtil, "@ZephyrIssue");
                if (!testScenarioNameTagsIssues.isEmpty()) {
                    testScenarioNameTagsIssues.forEach((name, tagsIssues) -> testCases.forEach(testCase -> {
                        if (name.equals(testCase.getName())) {
                            List<JiraIssue> jiraIssues;
                            try {
                                jiraIssues = jiraIssuesClient.getJiraIssue(propertiesUtil, tagsIssues);
                                List<Integer> issuesIds = jiraIssuesClient.getIssuesIds(jiraIssues);
                                testCasesClient.createIssueLink(propertiesUtil, testCase.getKey(), issuesIds);
                            } catch (URISyntaxException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }));
                } else {
                    System.out.println("Zephyr publisher: No tags found to add to Zephyr Scale test cases issues/stories.");
                }
            }
            System.out.println("Zephyr publisher: Test Cycle Location: " + testCycleResponse.getTestCycle().getUrl());

        } catch (IOException | URISyntaxException | NullPointerException e) {
            System.out.println("Zephyr publisher error: Failed to publish report to Zephyr Scale. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
