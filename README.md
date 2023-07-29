# Zephyr Scale API - Automation Results Publisher
The tool provides an opportunity to update test cases status and publish automation test results into Zephyr Scale Cloud.  
Detailed Automation API requests and properties can be found here: [Zephyr Scale API](https://support.smartbear.com/zephyr-scale-cloud/api-docs/#tag/Automations)  
How to generate ZEPHYR TOKEN: [Generating API Access Tokens](https://support.smartbear.com/zephyr-scale-cloud/docs/rest-api/generating-api-access-tokens.html)  

- [Properties configuration](#properties-configuration)
- [Map Test Results to Test Cases in Zephyr](#map-test-results-to-test-cases-in-zephyr)
    * [Cucumber](#cucumber)
    * [JUnit](#junit)
    * [TestNG](#testng)
- [Publish Cucumber Tags as Test Case Labels](#publish-cucumber-tags-as-test-case-labels)
- [Publish Cucumber Tags as Linked Jira Issues](#publish-cucumber-tags-as-linked-jira-issues)


## Properties configuration
1. Create ***zephyr.properties*** file in the ***resources*** folder of your project<br/>
2. Add the following properties:   
   ***Zephyr Scale connection details. E.g. for Cucumber report:***
      >baseUri=https://api.zephyrscale.smartbear.com/v2/  
       uriSuffix=automations/executions/cucumber  
       zephyrToken=<ZEPHYR_TOKEN> (better to provide it as a GitHub secret or an environment variable. E.g. for Windows: set ZEPHYR_TOKEN=xxxxxxxxxxx)  
       projectKey=<PROJECT_KEY>  
       autoCreateTestCases=true

   ***Results file details. E.g. for Cucumber report:***
      >resultsFolder=cucumber-report  
       resultsFileExtension=json

   ***Customize test cycle details (optional). If customTestCycle=false, testCycle params are ignored.***
      >customTestCycle=false  
       testCycleName=Test Cycle Name  
       testCycleDescription=Test Cycle Description  
       testCycleFolderName=Test Cycle Folder (make sure the folder name exists in Zephyr Scale)  
       testCycleJiraProjectVersion=1  
       customFields={}

   ***Get folders for TEST_CASE, TEST_PLAN or TEST_CYCLE. Used only when customTestCycle=true. Use TEST_CYCLE as default.***
      >folderType=TEST_CYCLE  
       maxResults=20

   **Switches to enable/disable update test cases with labels and/or stories (default=false)**
      >updateTestCasesWithLabels=false
       updateTestCasesWithStories=false

   **Jira Cloud properties**  
    NOTE: Used only to publish Cucumber tags as linked Jira issues. If not provided, this feature is ignored. 
      >jiraBaseUri=https://jira-eu-aholddelhaize.atlassian.net/rest/api/3/  
       jiraToken=<JIRA_TOKEN> (better to provide it as a GitHub secret or an environment variable. E.g. for Windows: set JIRA_TOKEN=xxxxxxxxxxxxxx)  
       jiraUserEmail=<JIRA_USER_EMAIL> (better to provide it as a GitHub secret or an environment variable. E.g. for Windows: set JIRA_USER_EMAIL=john.smith@ah.nl)

## Map Test Results to Test Cases in Zephyr

### Cucumber
Annotate the scenario by adding a tag such as `@TestCaseKey=ABC-T123`.  
In this example, Zephyr scale will then map the results of this Cucumber scenario to the corresponding test case with the key `ABC-T123`

```gherkin
Feature: Calculator
  
  @TestCaseKey=ABC-T123
  Scenario: Add a number
    Given a calculator I just turned on
    And I add 4 and 4
    Then the result is 8
```

**Note:** If the test Case doesn't exist yet, there is no need for annotation

### JUnit
Add annotations to your JUnit 4 tests for the test case key or name
In this example, Zephyr scale will then map the results of this JUnit test to the corresponding test case with the key `JQA-T1`

```java
public class CalculatorSumTest {

    @Test
    @TestCase(key = "JQA-T1")
    public void sumTwoNumbersAndPass() {
        Calculator calculator = new Calculator();
        assertEquals(1, calculator.sum(1, 2));
    }
}
```
**Note:** The rules for matching a test method to a test case are: 
* Try to match by using the test case key or name from the test method annotation @TestCase.
* If no test case is matched, then try to match a test case by using the method full qualified name.
* If no test case is matched, then:
  * If the parameter autoCreateTestCases is true, create the test case and create a new test execution for it.
  * If the parameter autoCreateTestCases is false or not present, return an error - no test cases have been matched.

### TestNG 

```java
public class ExampleTest {

    @Test(groups = {"group-1", "group-2"})
    public void DEV_T19_testMethod1() {
        assertThat(true).isTrue();
    }

    @Test()
    public void testMethod2_DEV_T21() {
        fail("failing test");
    }
}
```

**Note:** The first method name begins with `DEV_T19` and the second method name ends with `DEV_T21`.  
This particular prefix will map the result of each of those test methods to the corresponding test cases in Zephyr Scale 
if they exist, respectively test cases with key `DEV-T19` and `DEV-T21`.

The rules for matching a test method to a test case are:
* Try to match by parsing a test case key from the test method name.
* If no test case is matched, then try to match a test case by using the method full qualified name.
* If no test case is matched, then:
  * If the parameter autoCreateTestCases is true, create the test case and create a new test execution for it.
  * If the parameter autoCreateTestCases is false or not present, return an error - no test cases have been matched.


## Publish Cucumber Tags as Test Case Labels
The tool also allows to automatically update published test cases with labels.   
To do it, define Cucumber tags on feature or scenario level in the following format: 
`@ZephyrLabel=My_label`  
`My_label` will be automatically added to the test case labels.
```gherkin
Feature: Calculator

  @ZephyrLabel=My_label
  Scenario: Add a number
    Given a calculator I just turned on
    And I add 4 and 4
    Then the result is 8
```

## Publish Cucumber Tags as Linked Jira Issues
The tool also allows to automatically link any Jira issues to the published test cases.   
To do it, define Cucumber tags on feature or scenario level in the following format:
`@ZephyrIssue=My_Issue_Key` E.g. `@ZephyrIssue=QP-70`  
Issue with the key `QP-70` will be automatically linked to the test case Traceability -> Issues.
```gherkin
Feature: Calculator

  @ZephyrIssue=QP-70
  Scenario: Add a number
    Given a calculator I just turned on
    And I add 4 and 4
    Then the result is 8
```
