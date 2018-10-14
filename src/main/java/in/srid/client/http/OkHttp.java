package in.srid.client.http;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class OkHttp implements HttpClient {
    private final Request request;
    private final OkHttpClient client;

    public OkHttp(HttpConfig httpConfig) throws Exception {
        this.request = request(httpConfig.url());
        this.client = httpConfig.okHttpClient();
    }

    @Override
    public int makeRequest() {
        try (Response response = client.newCall(request).execute()) {
            return response.code();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        client.dispatcher().executorService().shutdown();
    }

    private static Request request(String uri) {
        MediaType json = MediaType.parse("application/json; charset=utf-8");
        RequestBody emptyRequestBody = RequestBody.create(json, new byte[]{});
        return new Request.Builder()
                .url(uri)
                .post(emptyRequestBody)
                .build();
    }
}
