package UITests.support;

import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpClientName;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.WebSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Blocking Selenium HTTP transport used as a fallback on Windows systems where
 * the JDK NIO selector cannot initialize its internal loopback connection.
 */
@HttpClientName("url-connection")
@SuppressWarnings("removal")
public final class UrlConnectionHttpClientFactory implements HttpClient.Factory {

    @Override
    public HttpClient createClient(ClientConfig config) {
        return new UrlConnectionHttpClient(config);
    }

    private static final class UrlConnectionHttpClient implements HttpClient {

        private final URI baseUri;
        private final Duration connectionTimeout;
        private final Duration readTimeout;

        private UrlConnectionHttpClient(ClientConfig config) {
            this.baseUri = config.baseUri();
            this.connectionTimeout = config.connectionTimeout();
            this.readTimeout = config.readTimeout();
        }

        @Override
        public HttpResponse execute(HttpRequest request) throws UncheckedIOException {
            HttpURLConnection connection = null;

            try {
                URI target = resolve(request);
                connection = (HttpURLConnection) target.toURL().openConnection(Proxy.NO_PROXY);
                connection.setConnectTimeout(Math.toIntExact(connectionTimeout.toMillis()));
                connection.setReadTimeout(Math.toIntExact(readTimeout.toMillis()));
                connection.setRequestMethod(request.getMethod().toString());
                connection.setInstanceFollowRedirects(false);

                HttpURLConnection finalConnection = connection;
                request.forEachHeader(finalConnection::addRequestProperty);

                byte[] requestBody = Contents.bytes(request.getContent());
                if (requestBody.length > 0) {
                    connection.setDoOutput(true);
                    connection.getOutputStream().write(requestBody);
                }

                int status = connection.getResponseCode();
                byte[] responseBody = readResponseBody(connection, status);

                HttpResponse response = new HttpResponse()
                        .setStatus(status)
                        .setContent(Contents.bytes(responseBody));

                for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                    if (header.getKey() == null || header.getValue() == null) {
                        continue;
                    }
                    for (String value : header.getValue()) {
                        response.addHeader(header.getKey(), value);
                    }
                }

                return response;
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        private URI resolve(HttpRequest request) {
            String requestUri = request.getUri();
            String query = request.getQueryString();

            if (query != null && !query.isBlank()) {
                requestUri += requestUri.contains("?") ? "&" + query : "?" + query;
            }

            return baseUri.resolve(requestUri);
        }

        private byte[] readResponseBody(HttpURLConnection connection, int status) throws IOException {
            InputStream stream = status >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (stream == null) {
                return new byte[0];
            }

            try (stream) {
                return stream.readAllBytes();
            }
        }

        @Override
        public WebSocket openSocket(HttpRequest request, WebSocket.Listener listener) {
            throw new UnsupportedOperationException("WebSocket is not required for WebDriver commands");
        }

        @Override
        public <T> CompletableFuture<java.net.http.HttpResponse<T>> sendAsyncNative(
                java.net.http.HttpRequest request,
                BodyHandler<T> handler
        ) {
            return CompletableFuture.failedFuture(
                    new UnsupportedOperationException("Native JDK HTTP transport is disabled")
            );
        }

        @Override
        public <T> java.net.http.HttpResponse<T> sendNative(
                java.net.http.HttpRequest request,
                BodyHandler<T> handler
        ) {
            throw new UnsupportedOperationException("Native JDK HTTP transport is disabled");
        }
    }
}
