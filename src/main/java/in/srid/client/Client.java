package in.srid.client;

import com.typesafe.config.Config;
import in.srid.client.http.AsyncHttp;
import in.srid.client.http.HttpAsync;
import in.srid.client.http.HttpClient;
import in.srid.client.http.HttpConfig;
import in.srid.client.http.HttpSync;
import in.srid.client.http.Noop;
import in.srid.client.http.OkHttp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Client {
    private static final AtomicBoolean firstTest = new AtomicBoolean(true);

    public static void startWith(Config appConfig) throws Exception {
        HttpConfig httpConfig = new HttpConfig(appConfig);
        Config loadTestConfig = appConfig.getConfig("client.loadTest");

        List<HttpClient> httpClients = Arrays.asList(
                new Noop(),
                new HttpSync(httpConfig),
                new HttpAsync(httpConfig),
                new AsyncHttp(httpConfig),
                new OkHttp(httpConfig)
        );

        httpClients.forEach(httpClient -> startLoadTest(httpClient, loadTestConfig));
    }

    private static void startLoadTest(HttpClient httpClient, Config loadTestConfig) {
        int sleepTime = loadTestConfig.getInt("intervalBetweenTestsInSeconds");
        takeSomeRest(sleepTime);

        try (LoadTest loadTest = new LoadTest(httpClient, loadTestConfig)) {
            loadTest.start();
        } catch (IOException ignored) {
        }
    }

    private static void takeSomeRest(int sleepTime) {
        if (!firstTest.getAndSet(false)) {
            sleepUninterruptibly(sleepTime, SECONDS);
        }
    }
}
