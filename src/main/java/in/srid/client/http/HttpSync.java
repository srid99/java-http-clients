package in.srid.client.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class HttpSync implements HttpClient {
    private final HttpPost post;
    private final CloseableHttpClient client;

    public HttpSync(HttpConfig httpConfig) throws Exception {
        this.post = new HttpPost(httpConfig.url());
        this.client = httpConfig.httpSyncClient();
    }

    @Override
    public int makeRequest() {
        try (CloseableHttpResponse response = client.execute(post)) {
            return response.getStatusLine().getStatusCode();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
