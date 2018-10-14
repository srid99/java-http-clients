package in.srid.client.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

public class HttpAsync implements HttpClient {
    private final HttpPost post;
    private final CloseableHttpAsyncClient client;

    public HttpAsync(HttpConfig httpConfig) throws Exception {
        this.post = new HttpPost(httpConfig.url());
        this.client = httpConfig.httpAsyncClient();
        this.client.start();
    }

    @Override
    public int makeRequest() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        AtomicInteger statusCodeHolder = new AtomicInteger();

        client.execute(post, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                statusCodeHolder.set(response.getStatusLine().getStatusCode());
                latch.countDown();
            }

            @Override
            public void failed(Exception ex) {
                exceptionHolder.set(ex);
                latch.countDown();
            }

            @Override
            public void cancelled() {
                latch.countDown();
            }
        });

        awaitUninterruptibly(latch);

        if (exceptionHolder.get() != null) {
            throw new RuntimeException(exceptionHolder.get());
        }

        return statusCodeHolder.get();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
