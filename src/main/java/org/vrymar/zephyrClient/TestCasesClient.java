package org.vrymar.zephyrClient;

import org.vrymar.model.testCase.TestCase;
import org.vrymar.utils.PropertiesUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.vrymar.utils.Parser;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Client to get test cases from Zephyr Scale Cloud
 */
public class TestCasesClient {

    private static final String URI_PATH_TESTCASES = "testcases/";
    private static final String URI_LOG_MESSAGE = "Zephyr publisher: URI to execute: ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String CONTENT_TYPE = "content-type";
    private static final String CONTENT_TYPE_APP_JSON = "application/json";

    /**
     * Get test case from Zephyr Scale Cloud
     * @param propertiesUtil  properties util tool to get properties
     * @param testCaseKey  test case key value
     * @return  test case
     * @throws URISyntaxException  URISyntaxException
     * @throws IOException  IOException
     */
    public TestCase getTestCase(PropertiesUtil propertiesUtil, String testCaseKey) throws URISyntaxException, IOException {
        TestCase testCase = null;
        String uri = propertiesUtil.getBaseUri() + URI_PATH_TESTCASES + testCaseKey;
        System.out.println(URI_LOG_MESSAGE + uri);

        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader(AUTHORIZATION, BEARER + propertiesUtil.getZephyrToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Zephyr publisher: Get Test Case response status code: " + responseCode);

            if (responseCode == 200) {
                testCase = Parser.tryParseResponse(response, TestCase.class);
            }
        }
        return testCase;
    }

    /**
     * Update test case in Zephyr Scale Cloud
     * @param propertiesUtil  properties util tool to get properties
     * @param testCaseKey  test case key value
     * @param testCase test case to update
     * @throws URISyntaxException  URISyntaxException
     * @throws IOException  IOException
     */
    public void updateTestCase(PropertiesUtil propertiesUtil, String testCaseKey, TestCase testCase) throws URISyntaxException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter jsonBody = new StringWriter();
        objectMapper.writeValue(jsonBody, testCase);

        String uri = propertiesUtil.getBaseUri() + URI_PATH_TESTCASES + testCaseKey;
        System.out.println(URI_LOG_MESSAGE + uri);
        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpPut putRequest = new HttpPut(uriBuilder.build());
        putRequest.setHeader(CONTENT_TYPE, CONTENT_TYPE_APP_JSON);
        putRequest.setHeader(AUTHORIZATION, BEARER + propertiesUtil.getZephyrToken());
        putRequest.setEntity(new StringEntity(jsonBody.toString()));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(putRequest)) {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Zephyr publisher: Update Test Case " + testCaseKey + " response status code: " + responseCode);

            if (responseCode == 200) {
                System.out.println("Zephyr publisher: Update Test Case " + testCaseKey + " is done successfully.");
            }
        }
    }

    /**
     * Add issue/story link to test case Zephyr Scale Cloud
     * @param propertiesUtil  properties util tool to get properties
     * @param testCaseKey  test case key value
     * @param issueIds issue/story id to link
     * @throws URISyntaxException  URISyntaxException
     * @throws IOException  IOException
     */
    public void createIssueLink(PropertiesUtil propertiesUtil, String testCaseKey, List<Integer> issueIds) throws URISyntaxException, IOException {
        String uri = propertiesUtil.getBaseUri() + URI_PATH_TESTCASES + testCaseKey + "/links/issues";
        System.out.println(URI_LOG_MESSAGE + uri);

        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpPost postRequest = new HttpPost(uriBuilder.build());
        postRequest.setHeader(CONTENT_TYPE, CONTENT_TYPE_APP_JSON);
        postRequest.setHeader(AUTHORIZATION, BEARER + propertiesUtil.getZephyrToken());

        for (int issueId : issueIds) {
            postRequest.setEntity(new StringEntity("{\"issueId\": " + issueId + "}"));
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int responseCode = response.getStatusLine().getStatusCode();
                System.out.println("Zephyr publisher: Create Test Case issue link response status code: " + responseCode);
            }
        }
    }

    /**
     * Add labels to test case
     * @param testCase  test case object
     * @param labels  list of labels
     * @return  test case object with labels
     */

    public TestCase buildTestCaseBodyWithLabels(TestCase testCase, List<String> labels) {
        testCase.setLabels(labels);
        return testCase;
    }
}
