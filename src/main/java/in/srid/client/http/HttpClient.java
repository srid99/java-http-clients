package in.srid.client.http;

import java.io.Closeable;

public interface HttpClient extends Closeable {
    int makeRequest();
}
