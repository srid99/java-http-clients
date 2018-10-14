package in.srid.client;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Stopwatch;
import com.typesafe.config.Config;
import in.srid.client.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.google.common.util.concurrent.Uninterruptibles.putUninterruptibly;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.range;

class LoadTest implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LoadTest.class);

    private static final MetricRegistry metric = new MetricRegistry();

    private final HttpClient httpClient;

    private final int noOfExecutions;
    private final int executionsPerBatch;

    private final boolean warmUpNeeded;
    private final int warmUpExecutions;

    private final ExecutorService executorService;

    LoadTest(HttpClient httpClient, Config loadTestConfig) {
        this.httpClient = httpClient;

        this.noOfExecutions = loadTestConfig.getInt("noOfExecutions");
        this.executionsPerBatch = loadTestConfig.getInt("executionsPerBatch");

        Config warmUpConfig = loadTestConfig.getConfig("warmUp");
        this.warmUpNeeded = warmUpConfig.getBoolean("needed");
        this.warmUpExecutions = warmUpConfig.getInt("noOfExecutions");

        this.executorService = newFixedThreadPool();
    }

    void start() {
        if (warmUpNeeded) {
            warmUp();
        }

        runTest();

        printReport();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    private void warmUp() {
        startExecuting(warmUpExecutions);
        metric.remove("execution");
        sleepUninterruptibly(1, SECONDS);
    }

    private void runTest() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOG.info("Start load test for '{}'", httpClient.getClass().getSimpleName());
        startExecuting(noOfExecutions);
        LOG.info("Test completed. '{}' took {}", httpClient.getClass().getSimpleName(), stopwatch);

        awaitTermination();
    }

    private void startExecuting(int noOfExecutions) {
        Timer timer = metric.timer("execution");

        range(0, noOfExecutions).boxed()
                .forEach(i -> executorService.submit(() -> {
                    try (Timer.Context ignored = timer.time()) {
                        int statusCode = httpClient.makeRequest();
                        LOG.debug("Status code: {}", statusCode);
                    } catch (Exception ex) {
                        LOG.error("Execution{} failed!", i, ex);
                    }
                }));
    }

    private ThreadPoolExecutor newFixedThreadPool() {
        int poolSize = executionsPerBatch;
        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                MILLISECONDS,
                new SynchronousQueue<>(),
                (runnable, executor) -> putUninterruptibly(executor.getQueue(), runnable)
        );
    }

    private void awaitTermination() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Integer.MAX_VALUE, MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void printReport() {
        ConsoleReporter.forRegistry(metric).build().report();
    }
}
