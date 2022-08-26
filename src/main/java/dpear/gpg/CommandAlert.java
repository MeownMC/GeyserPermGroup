package dpear.gpg;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.udojava.evalex.Expression;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class CommandAlert {

    //命令列表
    List <String>HardCommandAlert;
    List <String>SoftCommandAlert;
    ArrayList<Command> RegisterAlertCommands = new ArrayList<Command>();

    //实例化的时候获取主插件
    private final main plugin;
    private FileConfiguration config;

    public CommandAlert(main plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;

        //加载命令列表
        HardCommandAlert = config.getStringList("CommandAlert.Hard");
        SoftCommandAlert = config.getStringList("CommandAlert.Soft");

        //加载转接
        LoadCommandAlert();
    }

    public void ReloadConfig(FileConfiguration config){
        this.config = config;

        //加载命令列表
        HardCommandAlert = config.getStringList("CommandAlert.Hard");
        SoftCommandAlert = config.getStringList("CommandAlert.Soft");

        //加载转接
        LoadCommandAlert();
    }

    public boolean onPlayerCommand(Player player,String command){
        //解析命令
        String Command = command.split(" ")[0].substring(1);

        int index = SoftCommandAlert.indexOf(Command);
        if (index == -1) {
            //不匹配的话
            return false;
        }

        //获得参数
        String[] FullCMD;
        if(!command.substring(1).equals(Command)) {
            //有参数的话
            FullCMD = command.substring(Command.length() + 2).split(" ");
        }else {
            //没参数的话
            FullCMD = new String[]{};
        }

        //执行
        CommandAlertExecutor(player, Command,FullCMD);

        //返回
        return true;
    }


    public class EventListener implements Listener {

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent e) {

            //是否被其他插件取消
            if (e.isCancelled()) {
                return;
            }
            ;
        }


    }

    public boolean CommandAlertExecutor(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings){

        //获得命令路径
        String CommandPath = GetCommandAlertPath(s,strings,commandSender);

        //获得玩家实例
        Player p = Bukkit.getPlayer(commandSender.getName());

        //去点
        String CommandPathWithoutDot = CommandPath.substring(0,CommandPath.length()-1);

        //输出
        //getLogger().info("CommandPath:" + CommandPathWithoutDot);

        //判读表项是否存在
        if(!config.isConfigurationSection(CommandPathWithoutDot)) {
            commandSender.sendMessage("出现了内部异常，指定的数据不存在，请联系管理员解决");
            return true;
        }

        if (!config.getString(CommandPath + "Arg", "0").equals(String.valueOf(strings.length))) {
            //参数不足的话
            commandSender.sendMessage("参数数量错误");
            return false;
        }

        //判断是否是数字,防止注入
        for (Integer checknow:config.getIntegerList(CommandPath + "Require-Number")) {
            if(!Tools.isNumber(strings[checknow])){
                commandSender.sendMessage("非法参数");
                return false;
            }
        }

        List<String> ExecuteCommands = null;

        if (!config.getString(CommandPath + "Permission", "Null").equals("Null")) {
            if (!commandSender.hasPermission(config.getString(CommandPath + "Permission", "Null"))) {
                ExecuteCommands = config.getStringList(CommandPath + "Target-no-permission");
            }
        }

        if (p != null) {
            //玩家走表达式
            if (!config.getString(CommandPath + "Expression", "Null").equals("Null")) {
                String EXPString = Tools.ReplacePlaceholder(p,config.getString(CommandPath + "Expression", "Null"));

                //如果启用
                if (config.getBoolean(CommandPath + "Replace", false)) {
                    //替换参数
                    for (int i = 0; i < strings.length; i++) {
                        EXPString = EXPString.replace("{" + i + "}", strings[i]);
                    }
                }

                Expression expression = new Expression(EXPString);
                if (expression.eval().intValue() == 1) {
                    ExecuteCommands = config.getStringList(CommandPath + "Target");
                } else {
                    ExecuteCommands = config.getStringList(CommandPath + "Target-fail-expression");
                }
            }
        }


        //判断上面正不正常
        if (ExecuteCommands == null){
            ExecuteCommands = config.getStringList(CommandPath + "Target");
        }



        //执行
        if (config.getBoolean(CommandPath + "Replace", false)) {

            for (String executeCommand : ExecuteCommands) {

                //替换参数
                String Executer = executeCommand;
                for (int i = 0; i < strings.length; i++) {
                    Executer = Executer.replace("{" + i + "}", strings[i]);
                }

                //是否是玩家
                if (p != null) {
                    Executer = Executer.
                            replace("{PlayerName}", p.getName()).
                            replace("{PlayerUUID}", p.getUniqueId().toString()).
                            replace("{PlayerWorld}", p.getWorld().toString());
                }

                //执行命令
                plugin.tools.ExecuteWithoutPlaceholder(p,Tools.EvalexReplace(Tools.ReplacePlaceholder(p,Executer)));

            }

        } else {
            for (String executeCommand : ExecuteCommands) {
                //执行命令
                plugin.tools.Execute(p,executeCommand);
            }
        }

        return true;

    }

    public List<String> CommandAlertTabHandler(CommandSender sender, String s, String[] args) {
        try {

            //保存最后一项
            String LAST = args[args.length-1];
            //去除最后一项
            args[args.length-1] = "";

            List<String> TabResults = config.getStringList(GetCommandAlertPath(s,args,sender) + "Tab");

            if (TabResults.size() == 0) {
                //如果没写对应配置的话
                return null;
            }else{
                if(TabResults.get(0).equals("#Null")){
                    //不返回
                    return null;
                }
                if (TabResults.get(0).equals("#PlayerList")){
                    //玩家列表
                    return Tools.GetStringPlayerList(LAST);
                }

                //正常返回
                return (Tools.KeepStartWith (LAST, TabResults));
            }
        }catch (Exception e){
            //有问题就不返回
            return null;
        }

    }

    public String GetCommandAlertPath(String Command,String[] strings,CommandSender commandSender){

        StringBuilder sb = new StringBuilder();
        sb.append("CommandAlert.CommandList.").append(Command).append(".");

        //判断有没有Link
        String Link = config.getString(sb + "Link","");
        if (!Link.equals("")){
            sb.delete(0,sb.length() - 1);

            //这样应该没问题
            Command = Link;
            sb.append("CommandAlert.CommandList.").append(Command).append(".");

        }

        //获得玩家实例
        Player p = Bukkit.getPlayer(commandSender.getName());
        //确定是玩家支持
        if (p != null) {
            //版本
            if (config.getBoolean("CommandAlert.CommandList." + Command + ".exFunction.PlayerVersion", false)) {

                //获得版本
                String version = plugin.tools.GetVersion(p);

                //表项是否存在
                if(config.isConfigurationSection(sb + version)){
                    sb.append(version).append(".");
                }else{
                    sb.append("Other.");
                }

            }

            //权限组
            if (config.getBoolean("CommandAlert.CommandList." + Command + ".exFunction.PermissionGroup", false)) {

                //这边可能NullPointer
                try {

                    //只获取第一权限组
                    String permissiongroup = getServer().getServicesManager().getRegistration(Permission.class).getProvider().getPlayerGroups(p)[0];
                    //表项是否存在
                    if(config.isConfigurationSection(sb + permissiongroup)) {
                        sb.append(permissiongroup).append(".");
                    }else{
                        sb.append("Other.");
                    }


                }catch (Exception e){
                    getLogger().warning("在获取权限组时出现了异常");
                    getLogger().warning("以Other继续！");
                    sb.append("Other.");
                    e.printStackTrace();
                }
            }

            //玩家所在世界
            if (config.getBoolean("CommandAlert.CommandList." + Command + ".exFunction.PlayerWorld", false)) {

                //获得世界
                String world = p.getWorld().getName();

                //表项是否存在
                if(config.isConfigurationSection(sb + world)){
                    sb.append(world).append(".");
                }else{
                    sb.append("Other.");
                }
            }
        }

        //参数个数(显然这个不关玩家事)
        if (config.getBoolean("CommandAlert.CommandList." + Command + ".exFunction.ArgAmount", false)) {

            //获得长度
            String length = String.valueOf(strings.length);

            //表项是否存在
            if(config.isConfigurationSection(sb + length)) {
                sb.append(length).append(".");
            }else{
                sb.append("Other.");
            }
        }


        //自定义参数(显然这个也不关玩家事)
        if (config.getBoolean("CommandAlert.CommandList." + Command + ".exFunction.Arg", false)) {
            for (String c : strings) {
                //表项是否存在
                if (config.isConfigurationSection(sb + c)) {
                    //让参数不为空再添加
                    if (!c.equals("")) {
                        sb.append(c).append(".");
                    }
                }else{
                    sb.append("Other.");
                }
            }
        }
        return sb.toString();
    }

    public void LoadCommandAlert() {
        //注册转接命令

        //soft
        getLogger().info("[GeyserPermGroup] [CA] 载入指令转接[Soft]");
        SoftCommandAlert = config.getStringList("CommandAlert.Soft");
        getLogger().info("[GeyserPermGroup] [CA] 已注册" + SoftCommandAlert.size() + "个命令转接[Soft]");

        //hard
        getLogger().info("[GeyserPermGroup] [CA] 载入指令转接[Hard]");
        ArrayList Commands_PerAdd = new ArrayList<Command>();
        HardCommandAlert = config.getStringList("CommandAlert.Hard");

        for (String s : HardCommandAlert) {
            //创建Command实例
            Command PerAdd = new Command(s) {
                @Override
                public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
                    return(CommandAlertExecutor(commandSender,s,strings));
                }

                public List<String> tabComplete(CommandSender sender, String alias, String[] args){
                    return (CommandAlertTabHandler(sender, alias, args));
                }

            };

            //将Command实例添加到列表
            Commands_PerAdd.add(PerAdd);
        }

        //反射+置命令
        try {
            final Field bukkitCommandMap = getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap cm = (CommandMap) bukkitCommandMap.get(getServer());

            cm.registerAll("GeyserPermGroup", Commands_PerAdd);

            RegisterAlertCommands = Commands_PerAdd;

            getLogger().info("[GeyserPermGroup] [CA] 已注册" + HardCommandAlert.size() + "个命令转接[Hard]");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().warning("[GeyserPermGroup] 载入指令转接[Hard]失败！");
            getLogger().warning("[GeyserPermGroup] 出现了异常");
            e.printStackTrace();
        }
    }

    public void LoadCommandAlertTabComplete(){

        getLogger().info("载入指令转接补全[Hard]");
        int Success = 0;
        for (String s : HardCommandAlert){
            try {

                //Objects.requireNonNull(getCommand(s)).setTabCompleter(CommandAlertTabHandler());
                Success = Success + 1;
            }catch (Exception e){
                getLogger().warning("在注册命令" + s + "时出现异常:");
                e.printStackTrace();
            }
        }
        getLogger().info("已注册" + Success + "个命令转接补全[Hard]");
    }

    public void UnloadCommandAlert(){


        try {
            final Field bukkitCommandMap = getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap cm = (CommandMap) bukkitCommandMap.get(getServer());

            for (Command c : RegisterAlertCommands) {
                //检索command
                c.unregister(cm);
            }

            getLogger().info("卸载转接指令成功");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().warning("卸载转接指令失败！");
            getLogger().warning("出现了异常");
            e.printStackTrace();
        }

    }
}
