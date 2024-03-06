package org.vrymar.zephyrClient;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.vrymar.model.folders.Folders;
import org.vrymar.model.testCycle.TestCycleBuilder;
import org.vrymar.model.testCycle.TestCycleResponse;
import org.vrymar.utils.PropertiesUtil;
import org.vrymar.utils.Parser;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client to get test cycle data from Zephyr Scale Cloud
 */
public class TestCycleClient {
    private final CloseableHttpClient httpClient;

    public TestCycleClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Create test cycle with test data by post http request
     *
     * @param propertiesUtil properties util tool to get properties
     * @param zipFile        zip file with test data
     * @return http response
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public TestCycleResponse postTestCycle(PropertiesUtil propertiesUtil, File zipFile) throws IOException, URISyntaxException {
        String baseUri = propertiesUtil.getBaseUri();
        String uriSuffix = propertiesUtil.getUriSuffix();
        String zephyrToken = propertiesUtil.getZephyrToken();
        String projectKey = propertiesUtil.getProjectKey();
        boolean autoCreateTestCases = propertiesUtil.isAutoCreateTestCases();
        boolean customTestCycle = propertiesUtil.isCustomTestCycle();
        String testCycleName = propertiesUtil.getTestCycleName();
        String testCycleFolderName = propertiesUtil.getTestCycleFolderName();
        String testCycleDescription = propertiesUtil.getTestCycleDescription();
        int testCycleJiraProjectVersion = propertiesUtil.getTestCycleJiraProjectVersion();
        String customFields = propertiesUtil.getCustomFields();
        AtomicReference<TestCycleResponse> testCycleResponse = new AtomicReference<>();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("projectKey", projectKey));
        params.add(new BasicNameValuePair("autoCreateTestCases", String.valueOf(autoCreateTestCases)));

        String uri = baseUri + uriSuffix;
        System.out.println("Zephyr publisher: URI to execute: " + uri);
        URIBuilder uriBuilder = new URIBuilder(uri);
        uriBuilder.addParameters(params);

        HttpPost postRequest = new HttpPost(uriBuilder.build());
        postRequest.setHeader("Authorization", "Bearer " + zephyrToken);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(
                "file",
                zipFile,
                ContentType.DEFAULT_BINARY,
                zipFile.getName()
        );

        if (customTestCycle) {
            FoldersClient foldersClient = new FoldersClient(httpClient);
            Folders folders = foldersClient.getZephyrFolders(propertiesUtil);
            List<Integer> foldersIds = Folders.getZephyrFoldersIdsByName(folders, testCycleFolderName);
            String testCycle = TestCycleBuilder.generateTestCycle(testCycleName, testCycleDescription, foldersIds,
                    testCycleJiraProjectVersion, customFields);

            if (testCycle != null) {
                builder.addTextBody(
                        "testCycle",
                        testCycle,
                        ContentType.APPLICATION_JSON
                );
            }
        }

        HttpEntity multipart = builder.build();
        postRequest.setEntity(multipart);

        HttpClientResponseHandler<TestCycleResponse> responseHandler = response -> {
            int responseCode = response.getCode();
            System.out.println("Zephyr publisher: Post Test Cycle response status code: " + responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                testCycleResponse.set(Parser.tryParseResponse((CloseableHttpResponse) response, TestCycleResponse.class));
            } else {
                String responseBody = getResponseBodyAsString((CloseableHttpResponse) response);
                System.out.println("Zephyr publisher error: Response body: " + responseBody + ". \nPossible reasons: " +
                        "\n1. Test results file was not found/not created, thus, 'testResults.zip' is empty." +
                        "\n2. Test results file is in improper format, thus, POST request fails.");
                System.out.println("Zephyr publisher error note: response code 400 doesn't always mean the results were not published. " +
                        "Please check Zephyr Scale Tests and Cycles.");
            }
            return testCycleResponse.get();
        };


        TestCycleResponse execute = httpClient.execute(postRequest, responseHandler);
        return execute;
    }

    private String getResponseBodyAsString(CloseableHttpResponse response) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
