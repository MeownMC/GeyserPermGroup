package dpear.gpg;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.bukkit.Bukkit.getLogger;

public class TaskRunner {

    //实例化的时候获取主插件
    private final main plugin;
    private FileConfiguration config;

    //任务列表
    private HashMap<String, BukkitTask> taskMap = new HashMap<String, BukkitTask>();

    public TaskRunner(main plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void ReloadConfig(FileConfiguration config){
        this.config = config;
    }

    public void StartAllTask(){
        List<String> taskListString = config.getStringList("TaskRunner.EnabledTask");

        getLogger().info("[GeyserPermGroup] [TR] 正在加载" + taskListString.size() + "个[ALL]任务");

        for (String TaskNow:taskListString) {
            String CTarget = config.getString("TaskRunner.TaskList." + TaskNow + ".Target");
            List<String> Commands = config.getStringList("TaskRunner.TaskList." + TaskNow + ".Commands");
            boolean isAsync = config.getBoolean("TaskRunner.TaskList." + TaskNow + ".Async");
            int CTimer = config.getInt("TaskRunner.TaskList." + TaskNow + ".Timer");

            if (Objects.equals(CTarget, "Players")){

                //获取runnable
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {

                        //对于所有玩家执行
                        for (Player player:Bukkit.getOnlinePlayers()) {
                            //执行命令
                            for (String command:Commands) {
                                plugin.tools.Execute(player,command);
                            }
                        }
                    }
                };


                //执行runnable
                if (CTimer < 1){
                    if (isAsync){
                        taskMap.put(TaskNow,
                                runnable.runTaskLaterAsynchronously(plugin, -CTimer)
                        );
                    }else{
                        taskMap.put(TaskNow,
                                runnable.runTaskLater(plugin, -CTimer)
                        );
                    }
                }else{
                    if (isAsync){
                        taskMap.put(TaskNow,
                                runnable.runTaskTimerAsynchronously(plugin, 0, CTimer)
                        );
                    }else{
                        taskMap.put(TaskNow,
                                runnable.runTaskTimer(plugin, 0, CTimer)
                        );
                    }
                }


            }

            if (Objects.equals(CTarget, "Console")){

                //获取runnable
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        //执行命令
                        for (String command:Commands) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        }
                    }
                };


                //执行runnable
                if (CTimer < 1){
                    if (isAsync){
                        taskMap.put(TaskNow,
                                runnable.runTaskLaterAsynchronously(plugin, -CTimer)
                        );
                    }else{
                        taskMap.put(TaskNow,
                                runnable.runTaskLater(plugin, -CTimer)
                        );
                    }
                }else{
                    if (isAsync){
                        taskMap.put(TaskNow,
                                runnable.runTaskTimerAsynchronously(plugin, 0, CTimer)
                        );
                    }else{
                        taskMap.put(TaskNow,
                                runnable.runTaskTimer(plugin, 0, CTimer)
                        );
                    }
                }


            }
            
        }

        getLogger().info("[GeyserPermGroup] [TR] 任务加载完毕");

    }

    public void StopAllTask(){
        Collection<BukkitTask> btasks = taskMap.values();

        getLogger().info("[GeyserPermGroup] [TR] 正在取消" + btasks.size() + "个[ALL]任务");

        for (BukkitTask btask:btasks) {
            btask.cancel();
        }

        getLogger().info("[GeyserPermGroup] [TR] 任务取消完毕");
    }

    public void StopTask(String TaskName){
        getLogger().info("[GeyserPermGroup] [TR] 正在取消1个[" + TaskName + "]任务");
        BukkitTask btask = taskMap.get(TaskName);
        if (btask == null){
            getLogger().info("[GeyserPermGroup] [TR] 任务取消失败, 找不到任务");
            return;
        }
        btask.cancel();
        getLogger().info("[GeyserPermGroup] [TR] 任务取消完毕");
    }
}

