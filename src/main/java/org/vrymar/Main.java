package org.vrymar;

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
import java.util.HashMap;
import java.util.List;

public class Main {

    private static final String PROP_FILE = "zephyr.properties";

    public static void main(String[] args) {
        FileUtil fileUtil = new FileUtil();
        TestCycleClient publisher = new TestCycleClient();
        TestExecutionsClient testExecutionsClient = new TestExecutionsClient();
        TestCasesClient testCasesClient = new TestCasesClient();
        JiraIssuesClient jiraIssuesClient = new JiraIssuesClient();

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

            // Update Test Cases with Labels
            HashMap<String, List<String>> scenarioNameTagsLabels = fileUtil.getTestScenarioNameAndTagsFromResultsFile(propertiesUtil, "@ZephyrLabel");
            if (!scenarioNameTagsLabels.isEmpty()) {
                scenarioNameTagsLabels.forEach((name, tagsLabels) -> testCases.forEach(testCase -> {
                    if (name.equals(testCase.getName())) {
                        System.out.println("SCENARIO NAME CONTAINS: " + name);
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

            // Update Test Cases with Issues/Stories
            HashMap<String, List<String>> testScenarioNameTagsIssues = fileUtil.getTestScenarioNameAndTagsFromResultsFile(propertiesUtil, "@ZephyrIssue");
            if (!scenarioNameTagsLabels.isEmpty()) {
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

            System.out.println("Zephyr publisher: Test Cycle Location: " + testCycleResponse.getTestCycle().getUrl());

        } catch (IOException | URISyntaxException | NullPointerException e) {
            System.out.println("Zephyr publisher error: Failed to publish report to Zephyr Scale. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
