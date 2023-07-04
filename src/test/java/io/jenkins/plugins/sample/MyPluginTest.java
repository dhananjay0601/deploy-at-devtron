package io.jenkins.plugins.sample;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class MyPluginTest {

    private AbstractBuild<?, ?> build;
    private TaskListener listener;

    @Before
    public void setUp() {
        build = new AbstractBuild<>(mock(hudson.model.AbstractProject.class)) {};
        listener = new TaskListener() {
            private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override
            public PrintStream getLogger() {
                return new PrintStream(outputStream);
            }

            @Override
            public PrintStream getLogger(final Class<?> clazz) {
                return getLogger();
            }

            @Override
            public void annotate(final String text) {
                listener.getLogger().println(text);
            }

            @Override
            public void annotate(final String html, final String escapedHtml) {
                listener.getLogger().println(html);
            }
        };
    }

    @Test
    public void testOnCompleted() throws Exception {
        // Set up the necessary environment variables
        build.getEnvironment(listener).put("GIT_URL", "https://example.com/repo.git");
        build.getEnvironment(listener).put("GIT_BRANCH", "main");
        build.getEnvironment(listener).put("GIT_COMMIT", "abcdef123456");

        // Call the onCompleted method
        MyPlugin.GitDetailsListener gitDetailsListener = new MyPlugin.GitDetailsListener();
        gitDetailsListener.onCompleted((Run<?, ?>) build, listener);

        // Verify that the expected Git details are printed to the listener's logger
        assertEquals("------------------", listener.getLogger().toString().trim());
        assertEquals("Git Repository URL: https://example.com/repo.git", listener.getLogger().toString().trim());
        assertEquals("------------------", listener.getLogger().toString().trim());
        assertEquals("Branch: main", listener.getLogger().toString().trim());
        assertEquals("------------------", listener.getLogger().toString().trim());
        assertEquals("Commit Hash: abcdef123456", listener.getLogger().toString().trim());
        assertEquals("------------------", listener.getLogger().toString().trim());
        assertEquals("start-point-for-deployment", listener.getLogger().toString().trim());
    }

    @Test
    public void testPerformCurlRequest() throws Exception {
        // Set up the mock objects
        String url = "http://example.com";
        String apiToken = "my-token";
        String requestBody = "{\"key\":\"value\"}";

        // Create a mock HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Mock the input/output streams of the connection
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        connection.setOutputStream(outputStream);
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        // Call the performCurlRequest method
        MyPlugin.performCurlRequest(url, apiToken, requestBody, listener);

        // Verify that the connection is configured correctly
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("application/json", connection.getRequestProperty("Content-Type"));
        assertEquals(apiToken, connection.getRequestProperty("api-token"));

        // Verify the request body
        String requestBodySent = new String(outputStream.toByteArray());
        assertEquals(requestBody, requestBodySent);
    }
}
