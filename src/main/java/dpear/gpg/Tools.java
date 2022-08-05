package dpear.gpg;

import com.viaversion.viaversion.api.Via;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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

    public Tools(main plugin,FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;

        //判断版本
        try {
            if (Integer.parseInt(Bukkit.getMinecraftVersion()) >=18) {
                isHighVersion = true;
            }
        }catch (Exception ignored){}//别管他
    }

    public void ReloadConfig(FileConfiguration config){
        this.config = config;
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

        //打开菜单
        if (command.startsWith("Menu~")){
            plugin.bedrockMenu.SendFromConfig(player,command.substring(5),player);
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

}
