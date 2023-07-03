package io.jenkins.plugins.sample;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MyPlugin extends Builder implements SimpleBuildStep {

    @Extension
    public static class GitDetailsListener extends RunListener<Run<?, ?>> {

        @Override
        public void onCompleted(Run<?, ?> run, TaskListener listener) {
            if (run instanceof AbstractBuild) {
                AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) run;
                AbstractProject<?, ?> project = build.getProject();

                try {
                    // Get Git details from the build environment
                    String repositoryUrl = build.getEnvironment(listener).get("GIT_URL");
                    String branch = build.getEnvironment(listener).get("GIT_BRANCH");
                    String commitHash = build.getEnvironment(listener).get("GIT_COMMIT");

                    // Print Git details on the console output
                    listener.getLogger().println("------------------");
                    listener.getLogger().println("Git Repository URL: " + repositoryUrl);
                    listener.getLogger().println("------------------");
                    listener.getLogger().println("Branch: " + branch);
                    listener.getLogger().println("------------------");
                    listener.getLogger().println("Commit Hash: " + commitHash);
                    listener.getLogger().println("------------------");
                    listener.getLogger().println("start-point-for-deployment");

                    // Generic Webhook URL
                    String webhookUrl = "http://20.163.244.230:31757/orchestrator/webhook/ext-ci/1";
                    sendPayload("dhananjay0106/jenkins_ci_pipeline:14", webhookUrl, listener, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6IkFQSS1UT0tFTjpkaGFuYW5qYXkwMTA2L2plbmtpbnNfY2lfcGlwZWxpbmU6MTQiLCJpc3MiOiJhcGlUb2tlbklzc3VlciJ9.Gi1mecKaBq9f6_K0z33fpvcc22fR4dX3nBIn6GM7-2s");
                } catch (IOException | InterruptedException e) {
                    listener.getLogger().println("Failed to fetch Git details: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Hello " + "World " + "!!" );

        // Generic Webhook URL
        String webhookUrl = "http://20.163.244.230:31757/orchestrator/webhook/ext-ci/1";

        sendPayload("dhananjay0106/jenkins_ci_pipeline:15", webhookUrl, listener, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6IkFQSS1UT0tFTjpkaGFuYW5qYXkwMTA2L2plbmtpbnNfY2lfcGlwZWxpbmU6djEzIiwiaXNzIjoiYXBpVG9rZW5Jc3N1ZXIifQ.3ZedYA8Yincjr48if8gKob8unnCfFZrXCMoXSttxqFA");
    }

    public static void sendPayload(String imageName, String webhookUrl, TaskListener listener, String apiToken) throws IOException {

        String requestBody = "{\"dockerImage\":\"dhananjay0106/jenkins_ci_pipeline:v15\",\"digest\":\"test1\",\"dataSource\":\"ext\",\"materialType\":\"git\"}";
        performCurlRequest(webhookUrl, apiToken, requestBody, listener);
    }


    public static void performCurlRequest(String url, String apiToken, String requestBody, TaskListener listener) throws IOException {
        // Create the connection
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("api-token", apiToken);
            listener.getLogger().println(connection);
            // Write the request body to the connection's output stream
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] requestBodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(requestBodyBytes);
                outputStream.flush();
            }

            //Write the payload
            connection.getOutputStream().write(requestBody.getBytes());
            connection.getOutputStream().flush();

            // Get the response from the server
            int responseCode = connection.getResponseCode();
            listener.getLogger().println(requestBody);
            listener.getLogger().println(responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    listener.getLogger().println("Response: " + response.toString());
                }
            } else {
                listener.getLogger().println("Failed to send cURL request. Response code: " + responseCode);
            }
        } finally {
            // Close the connection
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
