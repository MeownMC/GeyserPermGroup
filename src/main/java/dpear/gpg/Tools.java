package dpear.gpg;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.udojava.evalex.Expression;
import com.viaversion.viaversion.api.Via;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tools {

    //实例化的时候获取配置文件
    private main plugin;
    private FileConfiguration config;

    //版本判断变量
    public boolean isHighVersion = false;

    //BC通信
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    public Tools(main plugin,FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;

        if (!config.getBoolean("OldMinecraftVersion",false)) {
            isHighVersion = true;
        }
    }



    public void ReloadConfig(FileConfiguration config){
        this.config = config;

        if (!config.getBoolean("OldMinecraftVersion",false)) {
            isHighVersion = true;
        }
    }

    public static List<String> KeepStartWith(String head, List<String> Strings){
        ArrayList<String> Wreturned = new ArrayList<>();
        for (String CheckNow:Strings) {
            if(CheckNow.startsWith(head)){
                //匹配
                Wreturned.add(CheckNow);
            }
        }
        return Wreturned;
    }

    public static List<String> GetStringPlayerList(String head){
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        ArrayList<String> players_string = new ArrayList<>();

        //转换
        if (head.equals("")){
            //直接添加
            for (Player player: players) {
                players_string.add(player.getName());
            }
        }else{
            //检查头相等
            for (Player player: players) {
                if (player.getName().startsWith(head)) {
                    players_string.add(player.getName());
                }
            }
        }
        return players_string;
    }

    public static float GetNote(int Note){
        return (float) Math.pow(2,(float)(Note-12)/12);
    };

    public static String ReplacePlaceholder(Player player,String input){
        return PlaceholderAPI.setPlaceholders(player,input).replace("\\n","\n");
    }

    public void Execute(Player player,String command){

        command = ReplacePlaceholder(player, command);

        //嵌套调用
        ExecuteWithoutPlaceholder (player,command);

    }

    public void ExecuteWithoutPlaceholder(Player player,String command){

        //以后台身份运行
        if (command.startsWith("Console~")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.substring(8));
            return;
        }

        //发送消息
        if (command.startsWith("Msg~")){
            player.sendMessage(command.substring(4));
            return;
        }

        //发送ActionBar
        if (command.startsWith("ActionBar~")){
            player.sendActionBar(command.substring(10));
            return;
        }

        //发送Title
        if (command.startsWith("Title~")){
            String[] Titles = command.substring(6).split(";");
            if (Titles.length == 2){
                player.sendTitle(Titles[0],Titles[1]);
                return;
            }
            //参数不够不当Title处理
        }

        //发送聊天
        if (command.startsWith("Chat~")){
            player.chat(command.substring(5));
            return;
        }

        //打开菜单
        if (command.startsWith("Menu~")){
            plugin.bedrockMenu.SendFromConfig(player,command.substring(5),player);
            return;
        }

        //BC切换
        if (command.startsWith("Server~")){
            SendPlayerBungee(player,command.substring(7));
            return;
        }

        //延迟执行
        if (command.startsWith("Delay~")){

            //分割
            String content = command.substring(6);
            int DelayEnd = content.indexOf ("~");

            //生成runable
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    //应该不会死循环
                    ExecuteWithoutPlaceholder(player,content.substring(DelayEnd + 1));
                }
            };

            //运行runable
            runnable.runTaskLater(plugin,Integer.parseInt(content.substring(0,DelayEnd)));
            return;

        }

        //执行命令
        Bukkit.dispatchCommand(player, command);

    }

    public String GetVersion(Player player){
        if(config.getBoolean("VersionCheck.FloodGate", false)){
            if(FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())){
                return ("Bedrock");
            };
        }

        if(config.getBoolean("VersionCheck.ViaVersion", false)){

            return (String.valueOf(
                    Via.getAPI().getPlayerVersion(player.getUniqueId())
            ));
        };

        return ("Unknow");
    }

    public void SendPlayerBungee(Player player,String serverName){
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public static String EvalexReplace(String input) {

        //处理命令
        int LastIndex = 0;

        while (true) {
            int StartIndex = input.indexOf("${", LastIndex);

            if (StartIndex == -1) {
                break;
            }

            int EndIndex = input.indexOf("}$", StartIndex);

            Expression expression = new Expression (input.substring(StartIndex + 2, EndIndex));

            input = input.substring(0, StartIndex) + expression.eval().intValue() + input.substring(EndIndex + 2);

        }

        return input;
    }

}
