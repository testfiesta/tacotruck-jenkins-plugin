package io.jenkins.plugins.tacotruck;

import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class TacotruckBuilderTest {

    final String runName = "Test Tacotruck Run";
    final String apiUrl = "https://api.example.com";
    final String handle = "testorg";
    final String provider = "provider";
    final String projectKey = "project";
    final String credentialsId = "credentialsId";
    final String resultsPath = "./results.xml";

    @Test
    void testBuild(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        TacotruckBuilder builder =
                new TacotruckBuilder(runName, apiUrl, provider, handle, projectKey, credentialsId, resultsPath);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(runName, build);
    }
}
