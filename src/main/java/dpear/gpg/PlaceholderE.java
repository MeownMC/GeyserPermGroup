package dpear.gpg;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;
import dpear.gpg.main;

import java.util.Arrays;

public class PlaceholderE extends PlaceholderExpansion{

    private final main plugin;
    public PlaceholderE(main plugin) {
        this.plugin = plugin;
    }

    //基础信息
    @Override
    public String getAuthor() {
        return "D-Pear";
    }

    @Override
    public String getIdentifier() {
        return "geyserpermgroup";
    }

    @Override
    public String getVersion() {
        return main.PluginVersion;
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }


    //实现
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.equalsIgnoreCase("name")) {
            return player == null ? null : player.getName(); // "name" requires the player to be valid
        }

        //获取玩家版本
        if(params.equalsIgnoreCase("version")) {
            return plugin.GetVersion(player.getPlayer());
        }

        //是否为基岩版玩家
        if(params.equalsIgnoreCase("isBedrock")) {
            return String.valueOf(FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId()));
        }

        return null; // 未知变量
    }

}
