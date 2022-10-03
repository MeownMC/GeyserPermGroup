package dpear.gpg;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class WebServer {

    //配置
    private int PORT = 9090;
    private final main plugin;
    private FileConfiguration config;

    //HttpServer
    HttpServer httpServer;

    //构造函数
    public WebServer(main plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        PORT = config.getInt("WebServer.Port");
    }

    //重载
    public void ReloadConfig(FileConfiguration config){
        this.config = config;
    }


    public boolean Enable(){
        try {

            InetSocketAddress a = new InetSocketAddress(PORT);

            //核心创建
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

            //开始监听
            httpServer.createContext("/", new TestHttpHandler());

            plugin.getLogger().info("[GeyserPermGroup] [WS] 网页服务器在端口" + PORT + "上启动");

            //启动
            httpServer.start();

            return true;

        }catch (Exception e){
            plugin.getLogger().info("[GeyserPermGroup] [WS] Socket Create failed! StackTrace:");
            e.printStackTrace();
            return false;
        }

    }

    public void Disable(){
        httpServer.stop(0);
        plugin.getLogger().info("[GeyserPermGroup] [WS] 网页服务器已关闭");
    }

    public class TestHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] reqPath = exchange.getRequestURI().toString().substring(1).split("/");

            //错误的情况
            if (reqPath.length != 2){
                exchange.sendResponseHeaders(404, 0);
            }

            //获取回执定义
            List<String> resContent = config.getStringList("WebServer.ResponseList." + reqPath[0]);

            //获取玩家名
            String PlayerName = reqPath[1];
            PlayerName = PlayerName.substring(0,PlayerName.length()-3);

            //获取玩家
            Player player = Bukkit.getPlayer(PlayerName);

            //没有这个回执定义
            if (resContent.size() == 0){
                exchange.sendResponseHeaders(404, 0);
            }

            //没有这个玩家
            if (player == null && !PlayerName.equals("~~~")){
                exchange.sendResponseHeaders(404, 0);
            }


            //创建StringBuilder
            StringBuilder sb = new StringBuilder();

            //生成内容
            for (String processNow:resContent) {
                String[] processInfo = processNow.split("➩");

                //如果不达标
                if (processInfo.length != 2){continue;}

                //达标添加这个语句
                sb.append("document.getElementById(\"").append(processInfo[0]).append("\").innerHTML=\"");
                sb.append(Tools.ReplacePlaceholder(player,processInfo[1])).append("\";");

            }

            //完成响应
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }


}

