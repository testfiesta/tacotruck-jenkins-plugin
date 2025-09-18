package io.jenkins.plugins.tacotruck;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class TacotruckCLIHelper {

    private static final Logger LOGGER = Logger.getLogger(TacotruckCLIHelper.class.getName());

    protected static CLIResult executeCLI(
            String[] command, Launcher launcher, TaskListener listener, FilePath workspace)
            throws InterruptedException {
        return executeCLI(command, launcher, listener, workspace, null);
    }

    protected static CLIResult executeCLI(
            String[] command, Launcher launcher, TaskListener listener, FilePath workspace, EnvVars envVars)
            throws InterruptedException {
        try {
            ArgumentListBuilder args = new ArgumentListBuilder();
            for (String arg : command) {
                args.add(arg);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ProcStarter ps = launcher.launch().pwd(workspace).cmds(args).stdout(outputStream);

            if (envVars != null) {
                ps = ps.envs(envVars);
            }

            int exitCode = ps.join();
            String output = outputStream.toString(StandardCharsets.UTF_8).trim();

            return new CLIResult(exitCode, output, exitCode == 0, null);

        } catch (IOException e) {
            String errorMsg = "✗ Failed to execute CLI command: " + e.getMessage();
            listener.getLogger().println(errorMsg);
            LOGGER.severe(errorMsg);
            return new CLIResult(-1, "", false, e.getMessage());
        }
    }

    protected static boolean isTacotruckCliAvailable(
            Launcher launcher, TaskListener listener, FilePath workspace, EnvVars envVars) {
        try {
            CLIResult result = executeCLI(
                    new String[] {"npx", "@testfiesta/tacotruck", "--version"}, launcher, listener, workspace, envVars);

            if (result.isSuccess()) {
                LOGGER.fine("✓ TacoTruck CLI is available via npx: " + result.getOutput());
                listener.getLogger().println("✓ TacoTruck CLI is available via npx: " + result.getOutput());
                return true;
            } else {
                if (result.getErrorMessage() != null) {
                    LOGGER.fine("✗ TacoTruck CLI not found via npx: " + result.getErrorMessage());
                    listener.getLogger().println("✗ TacoTruck CLI not found via npx: " + result.getErrorMessage());
                    listener.getLogger().println("Please ensure Node.js and npm are available in PATH");
                } else {
                    LOGGER.fine("✗ TacoTruck CLI check failed with exit code: " + result.getExitCode());
                    listener.getLogger()
                            .println("✗ TacoTruck CLI check failed with exit code: " + result.getExitCode());
                }
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.fine("TacoTruck CLI availability check was interrupted");
            listener.getLogger().println("✗ TacoTruck CLI availability check was interrupted");
            return false;
        }
    }

    protected static boolean isTacotruckCliAvailable(Launcher launcher, TaskListener listener, FilePath workspace) {
        return isTacotruckCliAvailable(launcher, listener, workspace, null);
    }

    protected static String findNpxPath(Launcher launcher, TaskListener listener, FilePath workspace, EnvVars envVars) {
        try {
            CLIResult result = executeCLI(new String[] {"which", "npx"}, launcher, listener, workspace, envVars);
            return result.isSuccess() ? result.getOutput() : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.fine("Finding npx path was interrupted");
            return null;
        }
    }

    protected static String getTacotruckCliVersion(
            Launcher launcher, TaskListener listener, FilePath workspace, EnvVars envVars) {
        try {
            String npxPath = findNpxPath(launcher, listener, workspace, envVars);
            CLIResult result = executeCLI(
                    new String[] {npxPath, "@testfiesta/tacotruck", "--version"},
                    launcher,
                    listener,
                    workspace,
                    envVars);

            return result.isSuccess() ? result.getOutput() : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.fine("Getting TacoTruck CLI version was interrupted");
            return null;
        }
    }

    protected static String getTacotruckCliVersion(Launcher launcher, TaskListener listener, FilePath workspace) {
        return getTacotruckCliVersion(launcher, listener, workspace, null);
    }

    protected static String[] buildSubmitCommand(
            String provider,
            String resultsPath,
            String projectKey,
            String apiToken,
            String handle,
            String runName,
            String baseUrl,
            String npxPath) {
        List<String> cmd = new ArrayList<>();

        cmd.add(npxPath);
        cmd.add("@testfiesta/tacotruck");
        cmd.add(provider);
        cmd.add("run:submit");

        cmd.add("--token");
        cmd.add(apiToken);
        cmd.add("--data");
        cmd.add(resultsPath);
        cmd.add("--organization");
        cmd.add(handle);
        cmd.add("--name");
        cmd.add(runName);
        cmd.add("--project-key");
        cmd.add(projectKey);
        cmd.add("--url");
        cmd.add(baseUrl);

        return cmd.toArray(new String[0]);
    }

    protected static boolean submitResults(
            String provider,
            String resultsPath,
            String projectKey,
            String apiToken,
            String handle,
            String runName,
            String baseUrl,
            Launcher launcher,
            TaskListener listener,
            FilePath workspace,
            EnvVars envVars) {

        listener.getLogger().println("Submitting test results to TacoTruck...");

        try {
            String npxPath = findNpxPath(launcher, listener, workspace, envVars);
            String[] command =
                    buildSubmitCommand(provider, resultsPath, projectKey, apiToken, handle, runName, baseUrl, npxPath);

            StringBuilder logCmd = new StringBuilder();
            for (int i = 0; i < command.length; i++) {
                if (i > 0) logCmd.append(" ");
                // Hide the token value for security
                if ("--token".equals(command[i]) && i + 1 < command.length) {
                    logCmd.append("--token ***");
                    i++; // Skip the actual token value
                } else {
                    logCmd.append(command[i]);
                }
            }
            listener.getLogger().println("Executing: " + logCmd.toString());

            CLIResult result = executeCLI(command, launcher, listener, workspace, envVars);

            listener.getLogger().println(result.getOutput());

            return result.isSuccess();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.getLogger().println("✗ Test result submission was interrupted");
            return false;
        }
    }

    protected static String getApiToken(String credentialsId) {
        if (credentialsId == null || credentialsId.trim().isEmpty()) {
            return null;
        }

        StringCredentials credentials = CredentialsHelper.lookupApiTokenCredentials(credentialsId);
        if (credentials != null) {
            return credentials.getSecret().getPlainText();
        }

        return null;
    }

    protected static boolean submitResultsWithCredentials(
            String provider,
            String resultsPath,
            String projectKey,
            String credentialsId,
            String handle,
            String runName,
            String baseUrl,
            Launcher launcher,
            TaskListener listener,
            FilePath workspace,
            EnvVars envVars) {

        String apiToken = getApiToken(credentialsId);
        if (apiToken == null) {
            LOGGER.severe("✗ Failed to retrieve API token from credentials: " + credentialsId);
            return false;
        }

        return submitResults(
                provider,
                resultsPath,
                projectKey,
                apiToken,
                handle,
                runName,
                baseUrl,
                launcher,
                listener,
                workspace,
                envVars);
    }
}
