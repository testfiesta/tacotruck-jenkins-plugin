package io.jenkins.plugins.tacotruck;

import static io.jenkins.plugins.tacotruck.Messages.*;

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

public class TacotruckBuilder extends Builder implements SimpleBuildStep {

    private final String runName;
    private final String apiUrl;
    private String project;
    private String credentialsId;
    private String provider;

    @DataBoundConstructor
    public TacotruckBuilder(String runName, String apiUrl, String provider, String project, String credentialsId) {
        this.runName = runName;
        this.apiUrl = apiUrl;
        this.provider = provider;
        this.project = project;
        this.credentialsId = credentialsId;
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

    public String getProvider() {
        return provider;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println(this.getRunName());
    }

    @Symbol("tacotruck")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0) return FormValidation.error(TacotruckBuilder_DescriptorImpl_errors_missingName());
            if (value.length() < 4) return FormValidation.warning(TacotruckBuilder_DescriptorImpl_warnings_tooShort());

            return FormValidation.ok();
        }

        public FormValidation doCheckApiUrl(@QueryParameter String value) throws IOException, ServletException {
            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String value) {
            if (value == null || value.isEmpty()) {
                return FormValidation.error("Please select valid credentials");
            }
            return FormValidation.ok();
        }

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
            return Messages.TacotruckBuilder_DescriptorImpl_DisplayName();
        }
    }
}
