package io.jenkins.plugins.tacotruck.service;

import java.io.IOException;
import java.net.http.HttpRequest;

public class TestrailHttpServiceImpl extends BaseHttpServiceImpl {

    public TestrailHttpServiceImpl(String apiUrl, String credentialsId) {
        super(apiUrl, credentialsId);
    }

    @Override
    protected HttpRequest buildHttpRequest(String requestBody) throws IOException {
        throw new UnsupportedOperationException("TestRail implementation not yet available");
    }

    @Override
    protected String buildRequestBody(String testRunName, String projectId) {
        throw new UnsupportedOperationException("TestRail implementation not yet available");
    }

    @Override
    protected String getAuthenticationHeader() throws IOException {
        throw new UnsupportedOperationException("TestRail implementation not yet available");
    }

    @Override
    protected String getSubmissionEndpoint() {
        throw new UnsupportedOperationException("TestRail implementation not yet available");
    }

    @Override
    public String getProviderName() {
        return "TestRail";
    }
}
