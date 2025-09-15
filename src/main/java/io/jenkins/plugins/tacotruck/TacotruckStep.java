package io.jenkins.plugins.tacotruck;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class TacotruckStep extends Step {

    private final String provider;
    private final String runName;
    private final String apiUrl;
    private final String handle;
    private final String credentialsId;
    private final String resultsPath;
    private String project;

    @DataBoundConstructor
    public TacotruckStep(
            String provider, String runName, String apiUrl, String handle, String credentialsId, String resultsPath) {
        this.provider = provider;
        this.runName = runName;
        this.apiUrl = apiUrl;
        this.handle = handle;
        this.credentialsId = credentialsId;
        this.resultsPath = resultsPath;
    }

    public String getProvider() {
        return provider;
    }

    public String getRunName() {
        return runName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getHandle() {
        return handle;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getResultsPath() {
        return resultsPath;
    }

    public String getProject() {
        return project;
    }

    @DataBoundSetter
    public void setProject(String project) {
        this.project = project;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new TacotruckStepExecution(this, context);
    }

    public static class TacotruckStepExecution extends SynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        private final transient TacotruckStep step;

        TacotruckStepExecution(TacotruckStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            StepContext context = getContext();
            FilePath workspace = context.get(FilePath.class);
            Launcher launcher = context.get(Launcher.class);
            TaskListener listener = context.get(TaskListener.class);
            EnvVars envVars = context.get(EnvVars.class);

            String version = TacotruckCLIHelper.getTacotruckCliVersion(launcher, listener, workspace, envVars);
            if (version == null) {
                throw new RuntimeException(
                        "TacoTruck CLI is not available and could not be installed automatically. "
                                + "Please ensure Node.js is available and npm has proper permissions for global installations.");
            }
            listener.getLogger().println("Using TacoTruck CLI version: " + version);
            listener.getLogger().println("Starting test result submission for run: " + step.getRunName());

            boolean success = TacotruckCLIHelper.submitResultsWithCredentials(
                    step.getProvider(),
                    step.getResultsPath(),
                    step.getProject(),
                    step.getCredentialsId(),
                    step.getHandle(),
                    step.getRunName(),
                    step.getApiUrl(),
                    launcher,
                    listener,
                    workspace);

            if (!success) {
                throw new RuntimeException("Failed to submit test results to TacoTruck");
            }

            listener.getLogger().println("✓ Test results successfully submitted to TacoTruck");
            return null;
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            Set<Class<?>> context = new HashSet<>();
            context.add(TaskListener.class);
            context.add(FilePath.class);
            context.add(Launcher.class);
            context.add(EnvVars.class);
            return context;
        }

        @Override
        public String getFunctionName() {
            return "tacotruck";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Submit test results to TacoTruck";
        }

        public FormValidation doCheckRunName(@QueryParameter String value) throws IOException, ServletException {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("Run name is required");
            }
            if (value.trim().length() < 3) {
                return FormValidation.warning("Run name should be at least 3 characters long");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApiUrl(@QueryParameter String value) throws IOException, ServletException {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("API URL is required");
            }
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                return FormValidation.error("API URL must start with http:// or https://");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckProvider(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("Provider is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckHandle(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("Organization handle is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckProject(@QueryParameter String value) {
            return FormValidation.ok();
        }

        public FormValidation doCheckResultsPath(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("Results path is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String value) {
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("Credentials are required - please select valid credentials");
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath final Item item, @QueryParameter final String credentialsId) {
            return CredentialsHelper.doFillCredentialsIdItems(item, credentialsId);
        }
    }
}
