package org.vrymar.jiraClient;

import org.vrymar.model.jiraIssue.JiraIssue;
import org.vrymar.utils.PropertiesUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.vrymar.utils.Parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Client to get Jira issues/stories
 */
public class JiraIssuesClient {

    /**
     * Get Jira issue/story
     * @param propertiesUtil  properties util tool to get properties
     * @param issueKeys  key of the issue to retrieve
     * @return  list of Jira issues/stories
     * @throws URISyntaxException  URISyntaxException
     * @throws IOException  IOException
     */
    public List<JiraIssue> getJiraIssue(PropertiesUtil propertiesUtil, List<String> issueKeys) throws URISyntaxException, IOException {
        List<JiraIssue> jiraIssues = new ArrayList<>();
        for (String issueKey : issueKeys) {
            String uri = propertiesUtil.getJiraBaseUri() + "issue/" + issueKey;
            System.out.println("Zephyr publisher: Jira URI to execute: " + uri);

            URIBuilder uriBuilder = new URIBuilder(uri);
            HttpGet getRequest = new HttpGet(uriBuilder.build());
            getRequest.setHeader("Accept", "application/json");
            getRequest.setHeader("Authorization", getBasicAuthenticationHeader(propertiesUtil.getJiraUserEmail(), propertiesUtil.getJiraToken()));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(getRequest)) {
                int responseCode = response.getStatusLine().getStatusCode();
                System.out.println("Zephyr publisher: Get Jira issue response status code: " + responseCode);

                switch (responseCode) {
                    case 200 -> {
                        JiraIssue jiraIssue = Parser.tryParseResponse(response, JiraIssue.class);
                        jiraIssues.add(jiraIssue);
                    }
                    case 404 -> System.out.println("Zephyr publisher: Warn: Create Test Case issue link failed. " +
                            "Please check if the issue exists in Jira Cloud.");
                    case 401 -> System.out.println("Zephyr publisher: Warn: Create Test Case issue link failed. " +
                            "Please check Jira Cloud properties in zephyr.properties file.");
                    default -> System.out.println("Zephyr publisher: Warn: Create Test Case issue link failed.");
                }
            }
        }
        return jiraIssues;
    }

    private String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    /**
     * Retrieve Jira IDs from Jira issue
     * @param jiraIssues list of Jira issues
     * @return list of Jira issues IDs
     */
    public List<Integer> getIssuesIds(List<JiraIssue> jiraIssues) {
        List<Integer> ids = new ArrayList<>();
        jiraIssues.forEach(issue -> {
            int id = Integer.parseInt(issue.getId());
            ids.add(id);
        });
        return ids;
    }
}
