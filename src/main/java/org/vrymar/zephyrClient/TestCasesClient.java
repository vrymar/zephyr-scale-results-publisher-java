package org.vrymar.zephyrClient;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.vrymar.model.testCase.TestCase;
import org.vrymar.utils.PropertiesUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.vrymar.utils.Parser;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client to get test cases from Zephyr Scale Cloud
 */
public class TestCasesClient {

    private static final String URI_PATH_TESTCASES = "testcases/";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String CONTENT_TYPE = "content-type";
    private static final String CONTENT_TYPE_APP_JSON = "application/json";
    private final CloseableHttpClient httpClient;

    /**
     * Constructor
     *
     * @param httpClient CloseableHttpClient
     */
    public TestCasesClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Get test case from Zephyr Scale Cloud
     *
     * @param propertiesUtil properties util tool to get properties
     * @param testCaseKey    test case key value
     * @return test case
     * @throws URISyntaxException URISyntaxException
     * @throws IOException        IOException
     */
    public TestCase getTestCase(PropertiesUtil propertiesUtil, String testCaseKey) throws URISyntaxException, IOException {
        AtomicReference<TestCase> testCase = new AtomicReference<>();
        String uri = propertiesUtil.getBaseUri() + URI_PATH_TESTCASES + testCaseKey;

        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader(AUTHORIZATION, BEARER + propertiesUtil.getZephyrToken());

        HttpClientResponseHandler<TestCase> responseHandler = response -> {
            int responseCode = response.getCode();
            System.out.println("Zephyr publisher: Get Test Case " + testCaseKey + " response status code: " + responseCode);

            if (responseCode == 200) {
                testCase.set(Parser.tryParseResponse((CloseableHttpResponse) response, TestCase.class));
            }
            return testCase.get();
        };

        return httpClient.execute(getRequest, responseHandler);
    }

    /**
     * Update test case in Zephyr Scale Cloud
     *
     * @param propertiesUtil properties util tool to get properties
     * @param testCaseKey    test case key value
     * @param testCase       test case to update
     * @throws URISyntaxException URISyntaxException
     * @throws IOException        IOException
     */
    public void updateTestCase(PropertiesUtil propertiesUtil, String testCaseKey, TestCase testCase) throws URISyntaxException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter jsonBody = new StringWriter();
        objectMapper.writeValue(jsonBody, testCase);

        String uri = propertiesUtil.getBaseUri() + URI_PATH_TESTCASES + testCaseKey;
        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpPut putRequest = new HttpPut(uriBuilder.build());
        putRequest.setHeader(CONTENT_TYPE, CONTENT_TYPE_APP_JSON);
        putRequest.setHeader(AUTHORIZATION, BEARER + propertiesUtil.getZephyrToken());
        putRequest.setEntity(new StringEntity(jsonBody.toString()));

        HttpClientResponseHandler<Integer> responseHandler = response -> {
            int responseCode = response.getCode();
            System.out.println("Zephyr publisher: Update Test Case " + testCaseKey + " response status code: " + responseCode);

            if (responseCode == 200) {
                System.out.println("Zephyr publisher: Update Test Case " + testCaseKey + " is done successfully.");
            }
            return responseCode;
        };

        httpClient.execute(putRequest, responseHandler);
    }

    /**
     * Create test script with steps in Zephyr Scale Cloud
     *
     * @param propertiesUtil      properties util tool to get properties
     * @param testCaseKey         test case key value
     * @param type                type of test script
     * @param testScriptWithSteps test script with steps
     * @throws URISyntaxException URISyntaxException
     * @throws IOException        IOException
     */
    public void createTestScriptWithSteps(PropertiesUtil propertiesUtil, String testCaseKey, String type, String testScriptWithSteps) throws URISyntaxException, IOException {
        String uri = propertiesUtil.getBaseUri() + URI_PATH_TESTCASES + testCaseKey + "/testscript";
        StringEntity jsonBody = new StringEntity(buildTestScriptWithStepsBody(type, testScriptWithSteps), ContentType.APPLICATION_JSON);
        HttpPost postRequest = new HttpPost(uri);
        postRequest.setHeader(CONTENT_TYPE, CONTENT_TYPE_APP_JSON);
        postRequest.setHeader(AUTHORIZATION, BEARER + propertiesUtil.getZephyrToken());
        postRequest.setEntity(jsonBody);

        HttpClientResponseHandler<Integer> responseHandler = response -> {
            int responseCode = response.getCode();
            System.out.println("Zephyr publisher: Create test script with steps in Test Case " + testCaseKey + " response status code: " + responseCode);
            return responseCode;
        };

        httpClient.execute(postRequest, responseHandler);
    }

    private String buildTestScriptWithStepsBody(String type, String textSteps) {
        return "{\"type\": \"" + type + "\", \"text\": \"" + textSteps + "\"}";
    }

    /**
     * Add issue/story link to test case Zephyr Scale Cloud
     *
     * @param propertiesUtil properties util tool to get properties
     * @param testCaseKey    test case key value
     * @param issueIds       issue/story id to link
     * @throws URISyntaxException URISyntaxException
     * @throws IOException        IOException
     */
    public void createIssueLink(PropertiesUtil propertiesUtil, String testCaseKey, List<Integer> issueIds) throws URISyntaxException, IOException {
        String uri = propertiesUtil.getBaseUri() + URI_PATH_TESTCASES + testCaseKey + "/links/issues";

        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpPost postRequest = new HttpPost(uriBuilder.build());
        postRequest.setHeader(CONTENT_TYPE, CONTENT_TYPE_APP_JSON);
        postRequest.setHeader(AUTHORIZATION, BEARER + propertiesUtil.getZephyrToken());

        for (int issueId : issueIds) {
            postRequest.setEntity(new StringEntity("{\"issueId\": " + issueId + "}"));

            HttpClientResponseHandler<Integer> responseHandler = response -> {
                int responseCode = response.getCode();
                System.out.println("Zephyr publisher: Create Test Case issue link response status code: " + responseCode);
                return responseCode;
            };

            httpClient.execute(postRequest, responseHandler);
        }
    }

    /**
     * Add labels to test case
     *
     * @param testCase test case object
     * @param labels   list of labels
     * @return test case object with labels
     */
    public TestCase buildTestCaseBodyWithLabels(TestCase testCase, List<String> labels) {
        testCase.setLabels(labels);
        return testCase;
    }
}
