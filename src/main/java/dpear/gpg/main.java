package dpear.gpg;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.Nullable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;



public class main extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("开始加载插件");
        saveDefaultConfig();
        if(getConfig().getBoolean("command.listener_PlayerJoinEvent", false)){
            getServer().getPluginManager().registerEvents(new EventListener(), this);
            getLogger().info("监听PlayerJoinEvent成功");
        }
        if (Bukkit.getPluginCommand("checkplayerbe") != null) {
            Bukkit.getPluginCommand("checkplayerbe").setExecutor(new Commander());
            getLogger().info("注册指令/checkplayerbe成功");
        }
        FileConfiguration config = getConfig();
        getLogger().info("插件加载完毕！感谢使用");
        getLogger().info("作者D-Pear QQ:1448360624");
    }

    public class EventListener implements Listener {
        @Nullable
        @EventHandler
        public void onPlayerLogin(PlayerLoginEvent Player) {
            if (Player.getPlayer().getUniqueId().toString().substring(0,18).equals("00000000-0000-0000")) {
                FileConfiguration config = getConfig();
                String chead = config.getString("command.head", "lp user ");
                String cfoot = config.getString("command.foot", " parent set bedefault");
                String ehead = config.getString("command.execute.head", "tell ");
                String efoot = config.getString("command.execute.foot", " Welcome to be.mewon.top");
                getLogger().info("玩家 " + Player.getPlayer().getName() + " 为Bedrock Edition玩家");
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console, chead + Player.getPlayer().getUniqueId() + cfoot);
                Bukkit.dispatchCommand(console, ehead + Player.getPlayer().getName() + efoot);
            } else {
                getLogger().info("玩家 " + Player.getPlayer().getName() + " 为Java Edition玩家");
            };
        }
    }
    public class Commander implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                return false;
            }
            ;
            if (args.length > 1) {
                return false;

            }
            ;
            Player P = Bukkit.getPlayer(args[0]);
            getLogger().info("玩家 " + P.getUniqueId() + " 加入");
            if (P.getUniqueId().toString().substring(0,18).equals("00000000-0000-0000")) {
                FileConfiguration config = getConfig();
                String chead = config.getString("command.head", "lp user ");
                String cfoot = config.getString("command.foot", " parent set bedefault");
                String ehead = config.getString("command.execute.head", "tell ");
                String efoot = config.getString("command.execute.foot", " Welcome to be.mewon.top");
                getLogger().info("玩家 " + P.getName() + " 为Bedrock Edition玩家");
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.dispatchCommand(console, chead + P.getUniqueId() + cfoot);
                Bukkit.dispatchCommand(console, ehead + P.getName() + efoot);
            } else {
                getLogger().info("玩家 " + P.getName() + " 为Java Edition玩家");
            };
            return true;
        }
    }
}
