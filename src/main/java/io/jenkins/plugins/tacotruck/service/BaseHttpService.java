package io.jenkins.plugins.tacotruck.service;

import hudson.model.TaskListener;
import java.io.IOException;

public interface BaseHttpService {

    boolean submitTestResults(String testRunName, String projectId, TaskListener listener) throws IOException;

    boolean validateConfiguration();

    String getProviderName();
}
