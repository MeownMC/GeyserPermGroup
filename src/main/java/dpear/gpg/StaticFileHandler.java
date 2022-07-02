package dpear.gpg;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.Cleanup;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;

import static org.bukkit.Bukkit.getLogger;


public class StaticFileHandler implements HttpHandler {


    /**
     * StaticFileHandler constructor.
     * @param baseDir The base directory of the static files.
     */

    /**
     * Handles the HTTP request.
     * @param ex The HTTP exchange.
     * @throws IOException If an error occurs.
     */
    @Override
    public void handle(HttpExchange ex) throws IOException {

        URI uri = ex.getRequestURI();
        String name = uri.toString();

        @Cleanup OutputStream out = ex.getResponseBody();

        String[] PATH = name.split("/");
        if (PATH.length == 3){
            ex.sendResponseHeaders(200, name.length());


            //out.write(
            //        PlaceholderAPI.setPlaceholders(
            //                Bukkit.getPlayer(PATH[1]),
            //                PATH[2].replace("%5E","%"))
            //                .getBytes()
            //);
            out.write(Files.readAllBytes(Paths.get("plugins/GeyserPermGroup/config.yml")));


        }else {
            ex.sendResponseHeaders(403, 0);
        }
    }

}
