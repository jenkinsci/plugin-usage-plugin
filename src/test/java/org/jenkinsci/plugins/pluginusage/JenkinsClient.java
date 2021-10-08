package org.jenkinsci.plugins.pluginusage;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.utils.URIBuilder;
import org.jenkinsci.plugins.pluginusage.api.PluginUsage;

import static org.junit.Assert.assertEquals;

public class JenkinsClient {

    private final int port;

    public JenkinsClient(int port) {
        this.port = port;
    }

    private URIBuilder getBaseURLBuilder() {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http");
        builder.setHost("localhost");
        builder.setPort(port);
        return builder;
    }

    private URL availablePluginsURL() throws URISyntaxException, MalformedURLException {
        return getBaseURLBuilder().setPath("/updateCenter/api/json").setParameter("depth", "1").build().toURL();
    }
    private URL installedPluginsURL() throws URISyntaxException, MalformedURLException {
        return getBaseURLBuilder().setPath("/pluginManager/api/json").setParameter("depth", "1").build().toURL();
    }
    private URL jobsURL() throws URISyntaxException, MalformedURLException {
        return getBaseURLBuilder().setPath("/api/json").build().toURL();
    }
    private URL pluginUsageApiURL() throws URISyntaxException, MalformedURLException {
        return getBaseURLBuilder().setPath("/pluginusage/api/json").setParameter("depth", "2").build().toURL();
    }
    private URL createJobURL(String name) throws URISyntaxException, MalformedURLException {
        return getBaseURLBuilder().setPath("/createItem").setParameter("name", name).build().toURL();
    }
    private URL installNecessaryPluginsURL() throws URISyntaxException, MalformedURLException {
        return getBaseURLBuilder().setPath("/pluginManager/installNecessaryPlugins").build().toURL();
    }
    private URL installPluginUsageURL() throws URISyntaxException, MalformedURLException {
        return getBaseURLBuilder().setPath("/pluginManager/uploadPlugin").build().toURL();
    }

    public List<String> getJobs() {

        try {
            HttpURLConnection con = (HttpURLConnection) jobsURL().openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            assertEquals(200, status);

            StringBuilder content = new StringBuilder();
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))){
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }
            con.disconnect();

            final JsonObject asJsonObject = JsonParser.parseString(content.toString()).getAsJsonObject();
            final JsonArray jobs = asJsonObject.get("jobs").getAsJsonArray();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(jobs.iterator(), Spliterator.ORDERED), false)
                    .map(element -> element.getAsJsonObject().get("name").getAsString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getInstalledPlugins() {

        try {
            HttpURLConnection con = (HttpURLConnection) installedPluginsURL().openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            assertEquals(200, status);

            StringBuilder content = new StringBuilder();
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))){
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }
            con.disconnect();

            final JsonObject asJsonObject = JsonParser.parseString(content.toString()).getAsJsonObject();
            final JsonArray jobs = asJsonObject.get("plugins").getAsJsonArray();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(jobs.iterator(), Spliterator.ORDERED), false)
                    .map(element -> element.getAsJsonObject().get("shortName").getAsString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getAvailablePlugins() {

        try {
            HttpURLConnection con = (HttpURLConnection) availablePluginsURL().openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            assertEquals(200, status);

            StringBuilder content = new StringBuilder();
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))){
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }
            con.disconnect();

            final JsonObject asJsonObject = JsonParser.parseString(content.toString()).getAsJsonObject();
            final JsonArray jobs = asJsonObject.get("availables").getAsJsonArray();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(jobs.iterator(), Spliterator.ORDERED), false)
                    .map(element -> element.getAsJsonObject().get("name").getAsString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PluginUsage getPluginUsage() {
        try {
            HttpURLConnection con = (HttpURLConnection) pluginUsageApiURL().openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();


            StringBuilder content = new StringBuilder();
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))){
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }
            con.disconnect();

            assertEquals(200, status);

            Gson gson = new Gson();
            return gson.fromJson(content.toString(), PluginUsage.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Void createJob(String name, String jobResource) {
        try {
            URLConnection con = createJobURL(name).openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try(final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(jobResource)){
                int nRead;
                byte[] data = new byte[4];

                while ((nRead = resourceAsStream.read(data, 0, data.length)) != -1) {
                    byteArrayOutputStream.write(data, 0, nRead);
                }
            }

            http.setFixedLengthStreamingMode(byteArrayOutputStream.size());
            http.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(byteArrayOutputStream.toByteArray());
            }
            int status = http.getResponseCode();


            StringBuilder content = new StringBuilder();
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))){
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }
            http.disconnect();
            assertEquals(200, status);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public Void installPlugins(String plugin, String version) {
        try {
            URLConnection con = installNecessaryPluginsURL().openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            byte[] out = ("<jenkins><install plugin=\""+ plugin + "@" + version + "\" /></jenkins>")
                    .getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            int status = http.getResponseCode();
            assertEquals(302, status);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Void installPluginUsage() {
        try{

            String charset = "UTF-8";
            final Path binaryFile = Paths.get("target", "plugin-usage-plugin.hpi");
            String boundary = Long.toHexString(System.currentTimeMillis());
            String CRLF = "\r\n";

            URLConnection connection = installPluginUsageURL().openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (
                    OutputStream output = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            ) {
                // Send binary file.
                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"").append(binaryFile.getFileName().toString()).append("\"").append(CRLF);
                writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(binaryFile.getFileName().toString())).append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(binaryFile, output);
                output.flush();
                writer.append(CRLF).flush();

                writer.append("--").append(boundary).append("--").append(CRLF).flush();
            }

            // Request is lazily fired whenever you need to obtain information about response.
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            assertEquals(200, responseCode);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
