package dpear.gpg;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.udojava.evalex.Expression;
import com.viaversion.viaversion.api.Via;
import de.themoep.minedown.MineDown;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class Tools {

    //实例化的时候获取配置文件
    private main plugin;
    private FileConfiguration config;

    //版本判断变量
    public boolean isHighVersion = false;

    //BC通信
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    public Tools(main plugin,FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;

        if (!config.getBoolean("OldMinecraftVersion",false)) {
            isHighVersion = true;
        }
    }



    public void ReloadConfig(FileConfiguration config){
        this.config = config;

        if (!config.getBoolean("OldMinecraftVersion",false)) {
            isHighVersion = true;
        }
    }

    public static List<String> KeepStartWith(String head, List<String> Strings){
        ArrayList<String> Wreturned = new ArrayList<>();
        for (String CheckNow:Strings) {
            if(CheckNow.toLowerCase().startsWith(head.toLowerCase())){
                //匹配
                Wreturned.add(CheckNow);
            }
        }
        return Wreturned;
    }

    public static List<String> GetStringPlayerList(String head){
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
                if (player.getName().toLowerCase().startsWith(head.toLowerCase())) {
                    players_string.add(player.getName());
                }
            }
        }
        return players_string;
    }

    public static float GetNote(int Note){
        return (float) Math.pow(2,(float)(Note-12)/12);
    };

    public static String ReplacePlaceholder(Player player,String input){
        return PlaceholderAPI.setPlaceholders(player,input).replace("\\n","\n");
    }

    public void Execute(Player player,String command, String[]... Args){

        command = ReplacePlaceholder(player, command);

        //嵌套调用
        ExecuteWithoutPlaceholder (player,command,Args);

    }

    public final void ExecuteWithoutPlaceholder(Player player, String command, String[]... Args){

        //以后台身份运行
        if (command.startsWith("Console~")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.substring(8));
            return;
        }

        //发送消息
        if (command.startsWith("Msg~")){
            player.sendMessage(MineDown.parse(command.substring(4)));
            return;
        }

        //发送ActionBar
        if (command.startsWith("ActionBar~")){
            player.sendActionBar(MineDown.parse(command.substring(10)));
            return;
        }

        //发送Title
        if (command.startsWith("Title~")){
            String[] Titles = command.substring(6).split(";");
            if (Titles.length == 2){
                player.sendTitle(Titles[0],Titles[1]);
                return;
            }
            //参数不够不当Title处理
        }

        //发送聊天
        if (command.startsWith("Chat~")){
            player.chat(command.substring(5));
            return;
        }

        //发送公屏
        if (command.startsWith("Broadcast~")){
            Bukkit.broadcast(MineDown.parse(command.substring(10)));
            return;
        }

        //打开菜单
        if (command.startsWith("Menu~")){
            plugin.bedrockMenu.SendFromConfig(player,command.substring(5),player);
            return;
        }

        //BC切换
        if (command.startsWith("Server~")){
            SendPlayerBungee(player,command.substring(7));
            return;
        }

        //写变量
        if (command.startsWith("Setvar~")){

            //分割
            String content = command.substring(7);
            int DelayEnd = content.indexOf ("~");

            //写变量
            plugin.variableCore.SetVariable(player.getUniqueId(),content.substring(0,DelayEnd),content.substring(DelayEnd + 1));
            return;
        }

        //直接调用配置
        if (command.startsWith("DLink~")){

            //是否传递原始参数
            if (Args.length == 1){

                //直接调用
                plugin.commandAlert.CommandAlertExecutorDirect(player,command.substring(6),Args[0]);
                return;
            }
        }

        //播放声音
        if (command.startsWith("PlaySound~")){
            //直接用PAPI播放
            PlaceholderAPI.setPlaceholders(player,"%Sound_" + command.substring(10) + "%");
            return;
        }

        //任务执行
        if (command.startsWith("Task~")){

            //生成runable
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    //应该不会死循环
                    ExecuteWithoutPlaceholder(player,command.substring(5));
                }
            };

            //运行runable
            runnable.runTask(plugin);
            return;
        }

        //延迟执行
        if (command.startsWith("Delay~")){

            //分割
            String content = command.substring(6);
            int DelayEnd = content.indexOf ("~");

            //生成runable
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    //应该不会死循环
                    ExecuteWithoutPlaceholder(player,content.substring(DelayEnd + 1));
                }
            };

            //运行runable
            runnable.runTaskLater(plugin,Integer.parseInt(content.substring(0,DelayEnd)));
            return;

        }



        //执行命令
        Bukkit.dispatchCommand(player, command);

    }

    public String GetVersion(Player player){
        if(config.getBoolean("VersionCheck.FloodGate", false)){
            if(FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())){
                return ("Bedrock");
            };
        }

        if(config.getBoolean("VersionCheck.ViaVersion", false)){

            return (String.valueOf(
                    Via.getAPI().getPlayerVersion(player.getUniqueId())
            ));
        };

        return ("Unknow");
    }

    public void SendPlayerBungee(Player player,String serverName){
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public static String EvalexReplace(String input) {

        //处理命令
        int LastIndex = 0;

        while (true) {
            int StartIndex = input.indexOf("${", LastIndex);

            if (StartIndex == -1) {
                break;
            }

            int EndIndex = input.indexOf("}$", StartIndex);

            Expression expression = new Expression (input.substring(StartIndex + 2, EndIndex));

            input = input.substring(0, StartIndex) + expression.eval().intValue() + input.substring(EndIndex + 2);

        }

        return input;
    }

    public void CheckSCID(){
        //获取机器码
        getLogger().info("[GeyserPermGroup] [SC] 正在获取CID");
        String OsName = System.getProperties().getProperty("os.name");

        String ComputerCode = "FAILURE";
        String ComputerCode_SHA = "";


        //如果使用Windows
        if (OsName.startsWith("Windows")){
            getLogger().info("[GeyserPermGroup] [SC] 您正在使用Windows");

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
            getLogger().info("[GeyserPermGroup] [SC] 您正在使用Linux/GNU");

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
            getLogger().warning("[GeyserPermGroup] [SC] CID获取失败");
            getLogger().warning("[GeyserPermGroup] [SC] 没有以管理员身份运行或不受支持的操作系统");
            ComputerCode = new Random().toString();
        }

        getLogger().info("[GeyserPermGroup] [SC] 正在加密CID");
        try {
            byte[] encrypted = MessageDigest.getInstance("SHA-256").digest(ComputerCode.getBytes(StandardCharsets.UTF_8));
            StringBuilder SHA = new StringBuilder();
            for (byte b : encrypted) {
                SHA.append(String.format("%02x", b));
            }
            ComputerCode_SHA = SHA.toString();
            getLogger().info("[GeyserPermGroup] [SC] 您的SCID: " + ComputerCode_SHA);

        }catch (Exception e){
            e.printStackTrace();
        }

        getLogger().info("[GeyserPermGroup] [SC] 正在比对SCID-C");
        checkSCIDC (ComputerCode_SHA);
        if (plugin.PassCheck){return;};
        checkSCIDC_TL (ComputerCode_SHA);

    }

    private void checkSCIDC(String ComputerCode_SHA){

        String ComputerCode_PMD = ComputerCode_SHA + plugin.getName() + plugin.PluginVersion + plugin.Developer;
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
            getLogger().warning("[GeyserPermGroup] [SC] SCID-C获取失败");
            ComputerCode_MD = new Random().toString();
        }
        if(config.getString("SCID-C", "Null").equals(ComputerCode_MD)){
            getLogger().info("[GeyserPermGroup] [SC] SCID-C校验成功");
            plugin.PassCheck = true;
        }
    }

    private void checkSCIDC_TL(String ComputerCode_SHA){

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int qur_ = 0;
        
        if (month <= 3){qur_ = 1;};
        if (month >= 4 && month <= 6){qur_ = 2;};
        if (month >= 7 && month <= 9){qur_ = 3;};
        if (month >= 10){qur_ = 4;};

        String ComputerCode_PMD = ComputerCode_SHA + plugin.getName() + plugin.PluginVersion + "|" + qur_ + plugin.Developer;
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

        if(config.getString("SCID-C", "Null").equals(ComputerCode_MD)){
            getLogger().info("[GeyserPermGroup] [SC] SCID-C校验成功");
            plugin.PassCheck = true;
        }else{
            getLogger().info("[GeyserPermGroup] [SC] SCID-C校验失败");
        }
    }

    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("^-?\\d+$");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public String VariableReplace(UUID PlayerUUID,String input) {

        //处理命令
        int LastIndex = 0;

        while (true) {
            int StartIndex = input.indexOf("#{", LastIndex);

            if (StartIndex == -1) {
                break;
            }

            int EndIndex = input.indexOf("}#", StartIndex);

            input = input.substring(0, StartIndex) +
                    plugin.variableCore.GetVariable(PlayerUUID,input.substring(StartIndex + 2, EndIndex)) +
                    input.substring(EndIndex + 2);

        }

        return input;
    }

}
