package io.jenkins.plugins.tacotruck;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class TacotruckBuilder extends Builder implements SimpleBuildStep {

    private final String runName;
    private final String apiUrl;
    private final String project;
    private final String handle;
    private final String credentialsId;
    private final String provider;
    private final String resultsPath;

    @DataBoundConstructor
    public TacotruckBuilder(
            String runName,
            String apiUrl,
            String provider,
            String handle,
            String project,
            String credentialsId,
            String resultsPath) {
        this.runName = runName;
        this.apiUrl = apiUrl;
        this.provider = provider;
        this.handle = handle;
        this.project = project;
        this.credentialsId = credentialsId;
        this.resultsPath = resultsPath;
    }

    public String getRunName() {
        return runName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getProject() {
        return project;
    }

    public String getHandle() {
        return handle;
    }

    public String getProvider() {
        return provider;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getResultsPath() {
        return resultsPath;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

        try {
            String version = TacotruckCLIHelper.getTacotruckCliVersion(launcher, listener, workspace, env);
            listener.getLogger().println("Using TacoTruck CLI version: " + version);
        } catch (Exception e) {
            throw new AbortException("TacoTruck CLI is not available and could not be installed automatically. "
                    + "Please ensure Node.js is available and npm has proper permissions for global installations.");
        }
        try {
            CLIResult result = TacotruckCLIHelper.submitResultsWithCredentials(
                    this.getProvider(),
                    this.getResultsPath(),
                    this.getProject(),
                    this.getCredentialsId(),
                    this.getHandle(),
                    this.getRunName(),
                    this.getApiUrl(),
                    launcher,
                    listener,
                    workspace,
                    env);

            if (!result.isSuccess()) {
                throw new AbortException("Failed to submit test results");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Symbol("tacotruck")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckRunName(@QueryParameter String value) throws IOException, ServletException {
            if (value == null || value.isBlank()) {
                return FormValidation.error("Run name is required");
            }
            if (value.trim().length() < 3) {
                return FormValidation.warning("Run name should be at least 3 characters long");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApiUrl(@QueryParameter String value) throws IOException, ServletException {
            if (value == null || value.isBlank()) {
                return FormValidation.error("API URL is required");
            }
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                return FormValidation.error("API URL must start with http:// or https://");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckProvider(@QueryParameter String value) {
            if (value == null || value.isBlank()) {
                return FormValidation.error("Provider is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckHandle(@QueryParameter String value) {
            if (value == null || value.isBlank()) {
                return FormValidation.error("Organization handle is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckProject(@QueryParameter String value) {
            if (value == null || value.isBlank()) {
                return FormValidation.error("Project is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckResultsPath(@QueryParameter String value) {
            if (value == null || value.isBlank()) {
                return FormValidation.error("Results path is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String value) {
            if (value == null || value.isBlank()) {
                return FormValidation.error("Credentials are required - please select valid credentials");
            }
            return FormValidation.ok();
        }

        @POST
        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath final Item item, @QueryParameter final String credentialsId) {
            return CredentialsHelper.doFillCredentialsIdItems(item, credentialsId);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Submit test results";
        }
    }
}
