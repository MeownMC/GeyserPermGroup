package dpear.gpg;

import dpear.gpg.events.spammer;
import org.bukkit.plugin.java.JavaPlugin;

public final class main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("欢迎使用 PearTools 扩展");
        getLogger().info("你的服务器真帅，我加载的时候都射了");
        // Register Events
        getServer().getPluginManager().registerEvents(new spammer(),this);
        getLogger().info("Spammer 全自动处罚模块成功加载");
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("服务器处于关闭阶段，正在卸载 PearTools 扩展");
        getLogger().info("你的服务器真帅，我卸载的时候都还没射完");
    }
}
