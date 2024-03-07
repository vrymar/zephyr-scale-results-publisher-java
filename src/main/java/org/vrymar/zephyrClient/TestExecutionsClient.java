package org.vrymar.zephyrClient;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.vrymar.model.testExecution.TestExecutions;
import org.vrymar.utils.PropertiesUtil;
import org.vrymar.utils.Parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client to work with test executions in Zephyr Scale Cloud
 */
public class TestExecutionsClient {

    private static final String URL_SPLITTER_REGEX = "/";
    private final CloseableHttpClient httpClient;

    public TestExecutionsClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Get test executions from Zephyr Scale Cloud with http request
     *
     * @param propertiesUtil properties util tool to get properties
     * @param testCycleKey   key of the test cycle
     * @return test execution
     * @throws URISyntaxException URISyntaxException
     * @throws IOException        IOException
     */
    public TestExecutions getTestExecutions(PropertiesUtil propertiesUtil, String testCycleKey) throws URISyntaxException, IOException {
        AtomicReference<TestExecutions> testExecutions = new AtomicReference<>();
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


        HttpClientResponseHandler<TestExecutions> responseHandler = response -> {
            int responseCode = response.getCode();
            System.out.println("Zephyr publisher: Get Zephyr Scale latest test execution response status code: " + responseCode);

            if (responseCode == 200) {
                testExecutions.set(Parser.tryParseResponse((CloseableHttpResponse) response, TestExecutions.class));
            }
            return testExecutions.get();
        };

        return httpClient.execute(getRequest, responseHandler);
    }

    /**
     * Retrieve scenarios key from test executions
     *
     * @param testExecutions test executions object
     * @return list of scenarios keys
     */
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
