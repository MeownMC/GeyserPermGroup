package dpear.gpg;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

import static org.bukkit.Bukkit.getLogger;


public class LocalServer {

    private HttpServer server;
    private String base = "plugins/Auto-Tune/web";

    /**
     * Start the local server.
     */
    public LocalServer() {

        // If "web-server-enabled" is false, don't start the server.

        try {

            server = HttpServer.create(new InetSocketAddress(8023), 0);
            server.createContext("/", new StaticFileHandler());
            server.setExecutor(null);
            server.start();

            getLogger().info("Local server started on port 8023");

        } catch (Exception e) {

            getLogger().warning(
                    "Error Creating Server on port: 8023. Please try restarting or changing your port.");

        }
    }

}