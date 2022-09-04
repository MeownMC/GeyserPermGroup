package dpear.gpg;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class PlaceholderExtension extends PlaceholderExpansion{

    public List<String> EnableSeasonWorlds = null;
    private final main plugin;
    public PlaceholderExtension(main plugin) {
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
        return plugin.PluginVersion;
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
            return plugin.tools.GetVersion(player.getPlayer());
        }

        //是否为基岩版玩家
        if(params.equalsIgnoreCase("isBedrock")) {
            return String.valueOf(FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId()));
        }

        //世界季节_到下个季节天数
        if(params.equalsIgnoreCase("rs_days_until_next_season")) {
            if(EnableSeasonWorlds.contains(player.getPlayer().getWorld().getName())) {
                return PlaceholderAPI.setPlaceholders(player, "%rs_days_until_next_season%" +"天");
            }else{
                return ("不可用");
            }
        }

        //世界季节_当前季节
        if(params.equalsIgnoreCase("rs_season")) {
            if(EnableSeasonWorlds.contains(player.getPlayer().getWorld().getName())) {
                return PlaceholderAPI.setPlaceholders(player, "%rs_season%");
            }else{
                return ("不可用");
            }
        }

        //世界季节_温度
        if(params.equalsIgnoreCase("rs_temperature")) {
            if(EnableSeasonWorlds.contains(player.getPlayer().getWorld().getName())) {
                return PlaceholderAPI.setPlaceholders(player, "%rs_temperature%");
            }else{
                return ("不可用");
            }
        }

        if (params.startsWith("ipr")){
            //是否启用
            if (!plugin.getConfig().getBoolean("EnabledFunction.IP2Region",false)) {
                return null;
            }
        }

        if(params.equalsIgnoreCase("ipr")) {
            try{
                return (plugin.ipsearch.search(player.getPlayer().getAddress().getAddress().getHostAddress()));
            }catch (Exception e){
                getLogger().info("查询ip失败");
                e.printStackTrace();
                return "";
            }
        }

        if(params.equalsIgnoreCase("ipr_0")) {
            try{
                String re = plugin.ipsearch.search(player.getPlayer().getAddress().getAddress().getHostAddress()).split("\\|")[0];
                if (re.equals("0")) {
                    return ("");
                }else {
                    return (re);
                }
            }catch (Exception e){
                getLogger().info("查询ip失败");
                e.printStackTrace();
                return "";
            }
        }

        if(params.equalsIgnoreCase("ipr_1")) {
            try{
                String re = plugin.ipsearch.search(player.getPlayer().getAddress().getAddress().getHostAddress()).split("\\|")[1];
                if (re.equals("0")) {
                    return ("");
                }else {
                    return (re);
                }
            }catch (Exception e){
                getLogger().info("查询ip失败");
                e.printStackTrace();
                return "";
            }
        }

        if(params.equalsIgnoreCase("ipr_2")) {
            try{
                String re = plugin.ipsearch.search(player.getPlayer().getAddress().getAddress().getHostAddress()).split("\\|")[2];
                if (re.equals("0")) {
                    return ("");
                }else {
                    return (re);
                }
            }catch (Exception e){
                getLogger().info("查询ip失败");
                e.printStackTrace();
                return "";
            }
        }

        if(params.equalsIgnoreCase("ipr_3")) {
            try{
                String re = plugin.ipsearch.search(player.getPlayer().getAddress().getAddress().getHostAddress()).split("\\|")[3];
                if (re.equals("0")) {
                    return ("");
                }else {
                    return (re);
                }
            }catch (Exception e){
                getLogger().info("查询ip失败");
                e.printStackTrace();
                return "";
            }
        }

        if(params.equalsIgnoreCase("ipr_4")) {
            try{
                String re = plugin.ipsearch.search(player.getPlayer().getAddress().getAddress().getHostAddress()).split("\\|")[4];
                if (re.equals("0")) {
                    return ("");
                }else {
                    return (re);
                }
            }catch (Exception e){
                getLogger().info("查询ip失败");
                e.printStackTrace();
                return "";
            }
        }

        if (params.startsWith("Evalex_")){
            return (Tools.EvalexReplace(params.substring(7)));
        }

        if (params.equalsIgnoreCase("rank")){
            String DTag = PlaceholderAPI.setPlaceholders(player, "%deluxetags_tag%");
            //判断有没有独立称号
            if (DTag.equals("")){
                //没有返回cmi的RANK
                return PlaceholderAPI.setPlaceholders(player, "%cmi_user_rank_displayname%");
            }else{
                //有返回独立称号
                return DTag;
            }
        }

        if (params.startsWith("var_")){
            String varMix = params.substring(4);
            String[] varSpl = varMix.split("~");
            if (varSpl.length == 2){
                //取某个UUID的变量
                return (plugin.variableCore.GetVariable(UUID.fromString(varSpl[0]),varSpl[1]));
            }else{
                //返回玩家的UUID
                return (plugin.variableCore.GetVariable(player.getUniqueId(),varMix));
            }
        }

        return null; // 未知变量
    }

}
