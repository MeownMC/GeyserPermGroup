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
        getLogger().info("你的服务器真帅，我加载的时候都射了");
        // Register Events
        getServer().getPluginManager().registerEvents(new spammer(),this);
        getLogger().info("Spammer 全自动处罚模块成功加载");
        saveConfig();
        getLogger().info("成功加载配置，食用愉快~");

        BukkitRunnable runbale = new BukkitRunnable() {
            @Override
            public void run() {
                throw new NullPointerException();
            }
        };

        getLogger().info("定时优化模块启动");
        runbale.runTaskTimer(this,100,1000);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("服务器处于关闭阶段，正在卸载 PearTools 扩展");
        getLogger().info("你的服务器真帅，我卸载的时候都还没射完");
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
