package org.vrymar.utils;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.vrymar.model.testResultCucumber.TestResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Parser class
 */
public class Parser {

    /**
     * Try to parse http response
     * @param response  CloseableHttpResponse
     * @param contentClass  class to map the data
     * @return  parsed object
     * @param <T>  type of the parsed object
     */
    public static <T> T tryParseResponse(CloseableHttpResponse response, Class<T> contentClass) {
        try (response) {
            String responseEntity = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
            return objectMapper.readValue(responseEntity, contentClass);
        } catch (IOException e) {
            System.out.println("Zephyr publisher error: Fail to parse response. Error: " + e.getMessage());
            return null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse Cucumber test result file
     * @param resultsFile result file
     * @return TestResult array object
     * @throws IOException  IOException
     */
    public TestResult[] parseCucumberTestResultFile(File resultsFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
        return objectMapper.readValue(resultsFile, TestResult[].class);
    }
}
