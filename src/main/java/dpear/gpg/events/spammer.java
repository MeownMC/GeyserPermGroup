package dpear.gpg.events;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

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
