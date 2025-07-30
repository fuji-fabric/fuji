package io.github.sakurawald.fuji.core.auxiliary;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

public class HttpUtil {

    public static final String THE_MOST_POPULAR_BROWSER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";

    private static void setConnectionProperties(HttpURLConnection connection) {
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", THE_MOST_POPULAR_BROWSER_AGENT);
    }

    public static String sendGetRequest(@NotNull String uri) throws IOException {
        @NotNull URI $uri = URI.create(uri);
        LogUtil.debug("Send a get request: uri = {}", $uri);

        HttpURLConnection connection = (HttpURLConnection) $uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        setConnectionProperties(connection);
        connection.setDoOutput(true);

        return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
    }

    public static String sendPostRequest(@NotNull String uri, @NotNull String param) throws IOException {
        @NotNull URI $uri = URI.create(uri);
        LogUtil.debug("Send a post request: uri = {}, param = {}", $uri, param);

        HttpURLConnection connection = (HttpURLConnection) $uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        setConnectionProperties(connection);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        IOUtils.write(param.getBytes(StandardCharsets.UTF_8), connection.getOutputStream());
        return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
    }
}
