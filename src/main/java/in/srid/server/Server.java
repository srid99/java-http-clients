package in.srid.server;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.typesafe.config.Config;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.google.common.io.Resources.getResource;

public class Server {
    private static final String OK_RESPONSE = "{\n" +
            "  \"success\": true\n" +
            "}";

    public static void startWith(Config appConfig) {
        int stubFixedDelay = appConfig.getInt("server.wiremock.fixedDelay");

        WireMockServer server = new WireMockServer(wireMockConfig(appConfig));
        server.stubFor(post(urlEqualTo("/test")).willReturn(json().withFixedDelay(stubFixedDelay)));
        server.start();
    }

    private static WireMockConfiguration wireMockConfig(Config appConfig) {
        Config httpConfig = appConfig.getConfig("http");
        Config sslConfig = httpConfig.getConfig("ssl");
        Config wireMockConfig = appConfig.getConfig("server.wiremock");
        Config jettyConfig = wireMockConfig.getConfig("jetty");

        int httpPort = httpConfig.getInt("httpPort");
        int httpsPort = httpConfig.getInt("httpsPort");

        String keystorePath = resource(sslConfig, "serverKeystoreLocation");
        String keystorePassword = sslConfig.getString("serverKeystorePassword");
        String truststorePath = resource(sslConfig, "truststoreLocation");
        String truststorePassword = sslConfig.getString("truststorePassword");

        return WireMockConfiguration.wireMockConfig()
                .port(httpPort)
                .httpsPort(httpsPort)
                .keystorePath(keystorePath)
                .keystorePassword(keystorePassword)
                .trustStorePath(truststorePath)
                .trustStorePassword(truststorePassword)
                .needClientAuth(jettyConfig.getBoolean("needClientAuth"))
                .containerThreads(jettyConfig.getInt("containerThreads"))
                .jettyAcceptors(jettyConfig.getInt("jettyAcceptors"))
                .asynchronousResponseEnabled(true)
                .asynchronousResponseThreads(jettyConfig.getInt("asynchronousResponseThreads"))
                .disableRequestJournal()
                .maxRequestJournalEntries(10)
                .notifier(new Slf4jNotifier(wireMockConfig.getBoolean("slf4jNotifier")));
    }

    private static String resource(Config config, String key) {
        String location = config.getString(key);
        return getResource(location).getFile();
    }

    private static ResponseDefinitionBuilder json() {
        return new ResponseDefinitionBuilder()
                .withBody(OK_RESPONSE)
                .withStatus(200)
                .withHeader("Content-Type", "application/json");
    }
}
