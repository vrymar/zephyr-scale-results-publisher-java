package org.vrymar.zephyrClient;

import org.vrymar.model.testExecution.TestExecutions;
import org.vrymar.utils.PropertiesUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.vrymar.utils.Parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TestExecutionsClient {

    private static final String URL_SPLITTER_REGEX = "/";

    public TestExecutions getTestExecutions(PropertiesUtil propertiesUtil, String testCycleKey) throws URISyntaxException, IOException {
        TestExecutions testExecutions = null;
        String uri = propertiesUtil.getBaseUri() + "testexecutions";
        System.out.println("Zephyr publisher: URI to execute: " + uri);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("projectKey", propertiesUtil.getProjectKey()));
        params.add(new BasicNameValuePair("testCycle", testCycleKey));
        params.add(new BasicNameValuePair("onlyLastExecutions", "true"));

        URIBuilder uriBuilder = new URIBuilder(uri);
        uriBuilder.addParameters(params);

        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader("Authorization", "Bearer " + propertiesUtil.getZephyrToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Zephyr publisher: Get Zephyr Scale latest test execution response status code: " + responseCode);

            if (responseCode == 200) {
                testExecutions = Parser.tryParseResponse(response, TestExecutions.class);
            }
        }
        return testExecutions;
    }

    public List<String> getScenariosKeys(TestExecutions testExecutions) {
        List<String> keys = new ArrayList<>();
        testExecutions.getValues().forEach(values -> {
            String url = values.getTestCase().getSelf();
            String[] array = url.split(URL_SPLITTER_REGEX);
            String key = array[array.length - 3];
            keys.add(key);
        });
        return keys;
    }
}
