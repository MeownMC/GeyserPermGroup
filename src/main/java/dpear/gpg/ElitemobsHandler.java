package dpear.gpg;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class ElitemobsHandler {

    //实例化的时候获取主插件
    private FileConfiguration config;


    //变量
    ArrayList<String> CustomBossesList;
    public ArrayList<UUID> CustomBossesUUID = new ArrayList(List.of());
    public ArrayList<String> CustomBossesName = new ArrayList<String>(List.of());


    public ElitemobsHandler(main plugin, FileConfiguration config) {
        this.config = config;

        //更新列表
        CustomBossesList = (ArrayList<String>) config.getStringList("EliteMobs.CustomBossesList");
    }

    public void ReloadConfig(FileConfiguration config){
        this.config = config;

        //更新列表
        CustomBossesList = (ArrayList<String>) config.getStringList("EliteMobs.CustomBossesList");
    }

}
