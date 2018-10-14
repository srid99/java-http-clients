package in.srid;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import in.srid.client.Client;
import in.srid.server.Server;

public class App {
    public static void main(String[] args) throws Exception {
        Config appConfig = ConfigFactory.load("app").getConfig("app");

        boolean startServer = args.length > 0 && args[0].equals("server");
        if (startServer) {
            Server.startWith(appConfig);
        } else {
            Client.startWith(appConfig);
        }
    }
}
