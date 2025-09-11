package io.jenkins.plugins.tacotruck.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class TestfiestaHttpServiceImpl extends BaseHttpServiceImpl {

    public TestfiestaHttpServiceImpl(String apiUrl, String credentialsId) {
        super(apiUrl, credentialsId);
    }

    @Override
    protected HttpRequest buildHttpRequest(String requestBody) throws IOException {
        return HttpRequest.newBuilder()
                .uri(URI.create(getSubmissionEndpoint()))
                .header("Content-Type", "application/json")
                // .header("Authorization", getAuthenticationHeader())
                .header("User-Agent", "Jenkins-Testfiesta-Plugin")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    @Override
    protected String buildRequestBody(String testRunName, String projectId) {
        String safeName = testRunName != null ? testRunName : "";
        String safeProjectId = projectId != null ? projectId : "";
        String timestamp = java.time.Instant.now().toString();

        return "{" + System.lineSeparator() + "    \"name\": \""
                + safeName + "\"," + System.lineSeparator() + "    \"project_id\": \""
                + safeProjectId + "\"," + System.lineSeparator() + "    \"source\": \"jenkins\","
                + System.lineSeparator() + "    \"created_at\": \""
                + timestamp + "\"," + System.lineSeparator() + "    \"status\": \"completed\""
                + System.lineSeparator() + "}";
    }

    @Override
    protected String getAuthenticationHeader() throws IOException {
        try {

            String credentials = "";

            if (credentials == null) {
                throw new IOException("Credentials not found with ID: " + credentialsId);
            }

            // String apiToken = credentials.getPassword().getPlainText();
            // return "Bearer " + apiToken;
            return "";

        } catch (Exception e) {
            throw new IOException("Failed to retrieve credentials", e);
        }
    }

    @Override
    protected String getSubmissionEndpoint() {
        return apiUrl.endsWith("/") ? apiUrl + "api/v1/test-runs" : apiUrl + "/api/v1/test-runs";
    }

    @Override
    public String getProviderName() {
        return "Testfiesta";
    }
}
