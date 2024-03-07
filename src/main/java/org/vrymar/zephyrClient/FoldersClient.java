package org.vrymar.zephyrClient;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.vrymar.model.folders.Folders;
import org.vrymar.utils.PropertiesUtil;
import org.vrymar.utils.Parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client to get folders from Zephyr Scale
 */
public class FoldersClient {
    private final CloseableHttpClient httpClient;

    /**
     * Constructor
     *
     * @param httpClient CloseableHttpClient
     */
    public FoldersClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Retrieve test cycle folders from Zephyr Scale
     *
     * @param propertiesUtil properties util tool to get properties
     * @return folders object
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public Folders getZephyrFolders(PropertiesUtil propertiesUtil) throws IOException, URISyntaxException {
        AtomicReference<Folders> folders = new AtomicReference<>();
        String uri = propertiesUtil.getBaseUri() + "folders";

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("projectKey", propertiesUtil.getProjectKey()));
        params.add(new BasicNameValuePair("folderType", propertiesUtil.getFolderType()));
        params.add(new BasicNameValuePair("maxResults", propertiesUtil.getMaxResults()));

        URIBuilder uriBuilder = new URIBuilder(uri);
        uriBuilder.addParameters(params);

        HttpGet getRequest = new HttpGet(uriBuilder.build());
        getRequest.setHeader("Authorization", "Bearer " + propertiesUtil.getZephyrToken());

        HttpClientResponseHandler<Folders> responseHandler = response -> {
            int responseCode = response.getCode();
            System.out.println("Zephyr publisher: Get Zephyr Scale folders response status code: " + responseCode);

            if (responseCode == 200) {
                folders.set(Parser.tryParseResponse((CloseableHttpResponse) response, Folders.class));
            }
            return folders.get();
        };

        return httpClient.execute(getRequest, responseHandler);
    }
}
