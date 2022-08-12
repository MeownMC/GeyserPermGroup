package dpear.gpg;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.List;

import static org.bukkit.Bukkit.*;

public class VersionCommand {

    //实例化的时候获取主插件
    private final main plugin;
    private FileConfiguration config;

    public VersionCommand(main plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void ReloadConfig(FileConfiguration config){
        this.config = config;
    }

    public boolean CheckAndExecute(Player player){
        //获取玩家版本
        String PlayerVersion = plugin.tools.GetVersion(player);

        getLogger().info("玩家 " + player.getName() + " 版本为 " + PlayerVersion);

        if(!config.getBoolean("Version." + PlayerVersion + ".Enable",false)){
            if(config.getBoolean("Version.Other.Enable",false)){
                PlayerVersion = "Other";
            }else{
                return true;
            }
        }

        //检测是否Link
        if (!config.getString("Version." + PlayerVersion + ".Link","").equals("")){
            PlayerVersion = config.getString("Version." + PlayerVersion + ".Link","");
            getLogger().info("版本连接至 " + PlayerVersion);
        }

        //计算密码
        int PASSWD = player.getUniqueId().hashCode();
        String PASSWORD_F = "Meown115141919810-Null" , PASSWORD_S = "";
        PASSWORD_F = PASSWD+"";
        PASSWORD_S = PASSWORD_F.substring(0,8);


        //是否启用AuthMe自动登入
        if (config.getBoolean("Version." + PlayerVersion + ".AutoLogin.AuthMe",false)){
            AuthMeApi b = AuthMeApi.getInstance();
            if(!b.isRegistered(player.getName())){

                //未注册，注册该玩家
                if (config.getBoolean("Version." + PlayerVersion + ".AutoLogin.AuthMe_CommandRegister",false)){
                    //使用命令注册
                    Bukkit.dispatchCommand(player,
                            config.getString("Command.Register" , "register %Password %Password").
                                    replace("%PlayerName", player.getName()).
                                    replace("%PlayerUUID", player.getUniqueId().toString()).
                                    replace("%Password", PASSWORD_S));
                }else{
                    //使用接口注册
                    b.registerPlayer(player.getName(),PASSWORD_S);
                }

                //执行命令
                WelcomePlayer(player,PlayerVersion,".OnPlayerRegister",PASSWORD_S);

                return true;
            }else{
                //已注册，登录
                b.forceLogin(player);
            }
        }

        //是否启用指令登录
        if (config.getBoolean("Version." + PlayerVersion + ".AutoLogin.Command",false)){
            Bukkit.dispatchCommand(player,
                    config.getString("Command.Login" , "login %Password").
                            replace("%PlayerName", player.getName()).
                            replace("%PlayerUUID", player.getUniqueId().toString()).
                            replace("%Password", PASSWORD_S)
            );

        };

        //执行命令
        WelcomePlayer(player,PlayerVersion,".OnPlayerJoin",PASSWORD_S);

        //判断权限组提供插件是否为null
        if (plugin.rsp == null){
            getLogger().info("权限组插件为null, 已自动修复:)");
            plugin.rsp = getServer().getServicesManager().getRegistration(Permission.class);
        }

        //修改权限组
        if (config.getBoolean("Version." + PlayerVersion + ".AutoPermissionGroup.Vault" , false)){
            String pgoal = config.getString("Version." + PlayerVersion + ".AutoPermissionGroup.Group" , "default");
            String now = plugin.rsp.getProvider().getPlayerGroups(player)[0];
            if(!now.equals(pgoal)){
                plugin.rsp.getProvider().playerRemoveGroup(player,now);
                plugin.rsp.getProvider().playerAddGroup(player,pgoal);
                getLogger().info("移除了玩家 " + player.getName() + " 的权限组" + now + "，添加至 " + pgoal);
            }
        }

        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            if (player.getName().charAt(0) == '.') {
                getLogger().warning("玩家 " + player.getName() + " 使用了非法用户名");

                //检查时候启动踢出
                if(config.getBoolean("Actions.AutoKick.Enable")) {
                    player.kickPlayer(config.getString("Actions.AutoKick.KickMessage" , "[Meown]You are NOT a bedrock player!"));
                    getLogger().info("玩家 " + player.getName() + " 已被踢出");
                };
            }
            ;
        };

        //延迟判断
        String finalPASSWORD_S = PASSWORD_S;
        BukkitRunnable Runable = new BukkitRunnable() {
            @Override
            public void run() {
                //外置玩家判断
                if (PlaceholderAPI.setPlaceholders(player,"%fastlogin_status%").equals("Premium")) {
                    WelcomePlayer(player, "FastLogin", "", finalPASSWORD_S);
                }
            }
        };
        Runable.runTaskLater(plugin,1500);

        return true;
    }

    private void WelcomePlayer(Player player, String version, String type, String password){

        //得到要发送的字符串的数组并发送
        List<String> Message = config.getStringList("Version." + version + ".Message" + type);
        for (String s : Message) {
            player.sendMessage(Tools.ReplacePlaceholder(player, s));
        }

        //得到要执行的命令并执行
        List<String> Commands = config.getStringList("Version." + version + ".Command" + type);
        for (String command : Commands) {
            plugin.tools.Execute(player,
                    command.
                            replace("%PlayerName", player.getName()).
                            replace("%PlayerUUID", player.getUniqueId().toString()).
                            replace("%Password", password));
        }

    }
}
