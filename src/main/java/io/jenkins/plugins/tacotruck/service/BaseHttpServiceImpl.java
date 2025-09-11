package io.jenkins.plugins.tacotruck.service;

import hudson.model.TaskListener;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class BaseHttpServiceImpl implements BaseHttpService {

    protected final String apiUrl;
    protected final String credentialsId;
    protected final HttpClient httpClient;

    protected BaseHttpServiceImpl(String apiUrl, String credentialsId) {
        this.apiUrl = apiUrl;
        this.credentialsId = credentialsId;
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    }

    @Override
    public boolean submitTestResults(String testRunName, String projectId, TaskListener listener) throws IOException {
        try {
            listener.getLogger().println("Submitting test results to " + getProviderName() + "...");

            String requestBody = buildRequestBody(testRunName, projectId);
            HttpRequest request = buildHttpRequest(requestBody);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccessResponse(response.statusCode())) {
                listener.getLogger().println("Test results submitted successfully to " + getProviderName());
                return true;
            } else {
                listener.getLogger()
                        .println("Failed to submit test results. Status: " + response.statusCode() + ", Response: "
                                + response.body());
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.getLogger().println("Test result submission was interrupted");
            return false;
        } catch (Exception e) {
            listener.getLogger().println("Error submitting test results: " + e.getMessage());
            throw new IOException("Failed to submit test results", e);
        }
    }

    @Override
    public boolean validateConfiguration() {
        return apiUrl != null
                && !apiUrl.trim().isEmpty()
                && credentialsId != null
                && !credentialsId.trim().isEmpty();
    }

    protected abstract HttpRequest buildHttpRequest(String requestBody) throws IOException;

    protected abstract String buildRequestBody(String testRunName, String projectId);

    protected abstract String getAuthenticationHeader() throws IOException;

    protected abstract String getSubmissionEndpoint();

    protected boolean isSuccessResponse(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
