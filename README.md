## HTTP clients

This project demonstrates the usage of different HTTP clients available in Java world.
It also includes a (basic) performance test setup, just in case if you like to test few cases based on your need.

This project also comes with a server (based on [wiremock](https://wiremock.org/), to simulate some delay) so that we can run our client against it.

HTTP clients used in this test,

* [Apache HTTP client](https://hc.apache.org/httpcomponents-client-4.5.x/index.html)
* [Apache HTTP Async client](https://hc.apache.org/httpcomponents-asyncclient-4.1.x/index.html)
* [Async HTTP client](https://github.com/AsyncHttpClient/async-http-client)
* [OkHttp client](https://square.github.io/okhttp/)


### How to run

Build the project using below command (we will use gradle to build and gradle wrapper will download gradle and build),

```
./gradlew clean build
```

This will produce tar/zip archives in `build/distributions` directory. Untar or unzip the archive. Go into the folder and run the below commands,

First start the server,

```
bin/java-http-clients server
```

Once the server is started, in another terminal, start the client,

```
bin/java-http-clients client
```

### Configuration

We use [config](https://lightbend.github.io/config/) and you can find the config file [here](src/main/config/app.conf). The certificates are generated with CN `localhost`.
So if you run the server and clients in different machines then you might have to create your own certificates.
