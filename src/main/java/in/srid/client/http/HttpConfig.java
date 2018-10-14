package in.srid.client.http;

import com.typesafe.config.Config;
import io.netty.handler.ssl.JdkSslContext;
import okhttp3.OkHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.ssl.SSLContexts;
import org.asynchttpclient.AsyncHttpClient;

import javax.net.ssl.SSLContext;
import java.net.URL;

import static com.google.common.io.Resources.getResource;
import static io.netty.handler.ssl.ClientAuth.REQUIRE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static okhttp3.internal.Util.platformTrustManager;
import static org.asynchttpclient.Dsl.config;

public class HttpConfig {
    private final Config config;

    private final String url;
    private final int maxConnection;
    private final int connectTimeout;
    private final int readTimeout;
    private final int requestTimeout;

    public HttpConfig(Config appConfig) {
        this.config = appConfig;

        Config httpClientConfig = appConfig.getConfig("client.httpClient");

        this.url = url(httpClientConfig);
        this.maxConnection = httpClientConfig.getInt("maxConnections");
        this.connectTimeout = httpClientConfig.getInt("connectTimeoutInMillis");
        this.readTimeout = httpClientConfig.getInt("readTimeoutInMillis");
        this.requestTimeout = httpClientConfig.getInt("requestTimeoutInMillis");
    }

    String url() {
        return url;
    }

    CloseableHttpClient httpSyncClient() throws Exception {
        return HttpClients.custom()
                .setMaxConnTotal(maxConnection)
                .setMaxConnPerRoute(maxConnection)
                .setDefaultRequestConfig(requestConfig())
                .setSSLContext(sslContext())
                .disableConnectionState()
                .build();
    }

    CloseableHttpAsyncClient httpAsyncClient() throws Exception {
        return HttpAsyncClients.custom()
                .setMaxConnTotal(maxConnection)
                .setMaxConnPerRoute(maxConnection)
                .setDefaultRequestConfig(requestConfig())
                .setSSLContext(sslContext())
                .disableConnectionState()
                .build();
    }

    AsyncHttpClient asyncHttpClient() throws Exception {
        JdkSslContext sslContext = new JdkSslContext(sslContext(), true, REQUIRE);
        return org.asynchttpclient.Dsl.asyncHttpClient(config()
                .setMaxConnections(maxConnection)
                .setMaxConnectionsPerHost(maxConnection)
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .setRequestTimeout(requestTimeout)
                .setSslContext(sslContext)
        );
    }

    OkHttpClient okHttpClient() throws Exception {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, MILLISECONDS)
                .readTimeout(readTimeout, MILLISECONDS)
                .sslSocketFactory(sslContext().getSocketFactory(), platformTrustManager())
                .hostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }

    private RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .build();
    }

    private SSLContext sslContext() throws Exception {
        Config sslConfig = config.getConfig("http.ssl");

        URL keystorePath = resource(sslConfig, "clientKeystoreLocation");
        char[] keystorePassword = sslConfig.getString("clientKeystorePassword").toCharArray();
        URL truststorePath = resource(sslConfig, "truststoreLocation");
        char[] truststorePassword = sslConfig.getString("truststorePassword").toCharArray();

        return SSLContexts.custom()
                .loadKeyMaterial(keystorePath, truststorePassword, keystorePassword)
                .loadTrustMaterial(truststorePath, truststorePassword, new TrustSelfSignedStrategy())
                .build();
    }

    private String url(Config httpClientConfig) {
        boolean https = httpClientConfig.getBoolean("https");

        String protocol = https ? "https" : "http";
        String hostname = config.getString("http.hostname");
        int port = https ? config.getInt("http.httpsPort") : config.getInt("http.httpPort");
        return String.format("%s://%s:%d/test", protocol, hostname, port);
    }

    private static URL resource(Config config, String key) {
        String location = config.getString(key);
        return getResource(location);
    }
}
