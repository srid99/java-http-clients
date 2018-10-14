package in.srid.client.http;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Noop implements HttpClient {
    @Override
    public int makeRequest() {
        sleepUninterruptibly(100, MILLISECONDS);
        return 0;
    }

    @Override
    public void close() {
    }
}
