package io.jenkins.plugins.tacotruck;

public class CLIResult {
    private final int exitCode;
    private final String output;
    private final boolean success;
    private final String errorMessage;

    public CLIResult(int exitCode, String output, boolean success, String errorMessage) {
        this.exitCode = exitCode;
        this.output = output;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
