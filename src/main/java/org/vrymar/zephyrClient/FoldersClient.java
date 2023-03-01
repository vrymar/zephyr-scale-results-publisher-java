package org.vrymar.zephyrClient;

import org.vrymar.model.folders.Folders;
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

public class FoldersClient {

    public Folders getZephyrFolders(PropertiesUtil propertiesUtil) throws IOException, URISyntaxException {
        Folders folders = null;
        String uri = propertiesUtil.getBaseUri() + "folders";

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("projectKey", propertiesUtil.getProjectKey()));
        params.add(new BasicNameValuePair("folderType", propertiesUtil.getFolderType()));
        params.add(new BasicNameValuePair("maxResults", propertiesUtil.getMaxResults()));

        URIBuilder uriBuilder = new URIBuilder(uri);
        uriBuilder.addParameters(params);

        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader("Authorization", "Bearer " + propertiesUtil.getZephyrToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Zephyr publisher: Get Zephyr Scale folders response status code: " + responseCode);

            if (responseCode == 200) {
                folders = Parser.tryParseResponse(response, Folders.class);
            }
        }
        return folders;
    }
}
