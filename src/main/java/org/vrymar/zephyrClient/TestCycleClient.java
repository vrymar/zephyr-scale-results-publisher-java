package org.vrymar.zephyrClient;

import org.vrymar.model.folders.Folders;
import org.vrymar.model.testCycle.TestCycleBuilder;
import org.vrymar.model.testCycle.TestCycleResponse;
import org.vrymar.utils.PropertiesUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.vrymar.utils.Parser;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TestCycleClient {

    public TestCycleResponse postTestCycle(PropertiesUtil propertiesUtil, File zipFile) throws IOException, URISyntaxException {
        String baseUri = propertiesUtil.getBaseUri();
        String uriSuffix = propertiesUtil.getUriSuffix();
        String zephyrToken = propertiesUtil.getZephyrToken();
        String projectKey = propertiesUtil.getProjectKey();
        String autoCreateTestCases = propertiesUtil.getAutoCreateTestCases();
        boolean customTestCycle = propertiesUtil.isCustomTestCycle();
        String testCycleName = propertiesUtil.getTestCycleName();
        String testCycleFolderName = propertiesUtil.getTestCycleFolderName();
        String testCycleDescription = propertiesUtil.getTestCycleDescription();
        int testCycleJiraProjectVersion = propertiesUtil.getTestCycleJiraProjectVersion();
        String customFields = propertiesUtil.getCustomFields();
        TestCycleResponse testCycleResponse = null;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("projectKey", projectKey));
        params.add(new BasicNameValuePair("autoCreateTestCases", autoCreateTestCases));

        String uri = baseUri + uriSuffix;
        System.out.println("Zephyr publisher: URI to execute: " + uri);
        URIBuilder uriBuilder = new URIBuilder(uri);
        uriBuilder.addParameters(params);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
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
                FoldersClient foldersClient = new FoldersClient();
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

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int responseCode = response.getStatusLine().getStatusCode();
                System.out.println("Zephyr publisher: Post Test Cycle response status code: " + responseCode);

                if (responseCode >= 200 && responseCode < 300) {
                    testCycleResponse = Parser.tryParseResponse(response, TestCycleResponse.class);
                } else {
                    String responseBody = getResponseBodyAsString(response);
                    System.out.println("Zephyr publisher error: Response body: " + responseBody);
                    System.out.println("Zephyr publisher error note: response code 400 doesn't always mean the results were not published. " +
                            "Please check Zephyr Scale Tests and Cycles.");
                }
            }
        }
        return testCycleResponse;
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
