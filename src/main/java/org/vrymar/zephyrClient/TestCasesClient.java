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

public class TestCasesClient {

    public TestCase getTestCase(PropertiesUtil propertiesUtil, String testCaseKey) throws URISyntaxException, IOException {
        TestCase testCase = null;
        String uri = propertiesUtil.getBaseUri() + "testcases/" + testCaseKey;
        System.out.println("Zephyr publisher: URI to execute: " + uri);

        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader("Authorization", "Bearer " + propertiesUtil.getZephyrToken());

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

    public void updateTestCase(PropertiesUtil propertiesUtil, String testCaseKey, TestCase testCase) throws URISyntaxException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        StringWriter jsonBody = new StringWriter();
        objectMapper.writeValue(jsonBody, testCase);

        String uri = propertiesUtil.getBaseUri() + "testcases/" + testCaseKey;
        System.out.println("Zephyr publisher: URI to execute: " + uri);
        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpPut putRequest = new HttpPut(uriBuilder.build());
        putRequest.setHeader("content-type", "application/json");
        putRequest.setHeader("Authorization", "Bearer " + propertiesUtil.getZephyrToken());
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

    public void createIssueLink(PropertiesUtil propertiesUtil, String testCaseKey, List<Integer> issueIds) throws URISyntaxException, IOException {
        String uri = propertiesUtil.getBaseUri() + "testcases/" + testCaseKey + "/links/issues";
        System.out.println("Zephyr publisher: URI to execute: " + uri);

        URIBuilder uriBuilder = new URIBuilder(uri);
        HttpPost postRequest = new HttpPost(uriBuilder.build());
        postRequest.setHeader("content-type", "application/json");
        postRequest.setHeader("Authorization", "Bearer " + propertiesUtil.getZephyrToken());

        for (int issueId : issueIds) {
            postRequest.setEntity(new StringEntity("{\"issueId\": " + issueId + "}"));
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int responseCode = response.getStatusLine().getStatusCode();
                System.out.println("Zephyr publisher: Create Test Case issue link response status code: " + responseCode);
            }
        }
    }

    public TestCase buildTestCaseBodyWithLabels(TestCase testCase, List<String> labels) {
        testCase.setLabels(labels);
        return testCase;
    }
}
