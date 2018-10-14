package in.srid.client.http;

import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;

public class AsyncHttp implements HttpClient {
    private final String url;
    private final AsyncHttpClient client;

    public AsyncHttp(HttpConfig httpConfig) throws Exception {
        this.url = httpConfig.url();
        this.client = httpConfig.asyncHttpClient();
    }

    @Override
    public int makeRequest() {
        return client
                .preparePost(url)
                .execute()
                .toCompletableFuture()
                .join()
                .getStatusCode();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
