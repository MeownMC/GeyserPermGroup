package dpear.gpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class VariableCore {
    //储存核心
    private HashMap<UUID, HashMap<String,String>> PlayerMap = new HashMap<>();
    private HashMap<String,String> TriggerMap = new HashMap<>();

    //自动保存
    public final BukkitRunnable AutoSave = new BukkitRunnable() {
        @Override
        public void run() {
            if (SaveHashMapToFile()){
                //成功保存
                getLogger().info("[GeyserPermGroup] [VC] Variable Saved");
            }else{
                //保存失败
                getLogger().warning("[GeyserPermGroup] [VC] Cannot Save Variable!");
            }
        }
    };

    public String GetVariable (UUID PlayerUUID,String VariableName){
        //有异常直接额返回空
        try {
            String RETURN = PlayerMap.get(PlayerUUID).get(VariableName);

            //为null不返回东西
            if (RETURN.equals("null")){
                return ("");
            }else {
                return (RETURN);
            }

        }catch (Exception e){
            return "";
        }
    }

    public boolean SetVariable (UUID PlayerUUID,String VariableName,String Content){
        try {

            //是否存在对应玩家
            if (PlayerMap.containsKey(PlayerUUID)) {

                //写入
                PlayerMap.get(PlayerUUID).put(VariableName, Content);

            } else {

                //不存在创建玩家
                HashMap<String, String> PlayerVar = new HashMap<String, String>();

                //写入
                PlayerVar.put(VariableName, Content);

                //放回
                PlayerMap.put(PlayerUUID,PlayerVar);
            }

            //检查触发器
            Trig (PlayerUUID,VariableName);

            //成功返回true
            return true;

        }catch (Exception e){

            //有错误返回false
            return false;

        }
    }

    public void ClearAll (){
        PlayerMap.clear();
    }

    public boolean SetHashMap (HashMap<UUID, HashMap<String,String>> Source){
        //有异常就返回false
        try {
            PlayerMap = Source;
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public HashMap<UUID, HashMap<String,String>> GetHashMap (){
        return PlayerMap;
    }

    public boolean SaveHashMapToFile(){
        try {
            File file = new File("plugins/GeyserPermGroup/Variables.dat");
            //写出到文件
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(new Inner(PlayerMap,TriggerMap));
            oos.flush();
            oos.close();
            return true;
        }catch (Exception e){
            getLogger().warning("[GeyserPermGroup] [VC] 变量文件写入失败, 尝试兼容模式");

            //尝试兼容就版本配置
            try {
                File file = new File("plugins/GeyserPermGroup/Variables.dat");
                //写出到文件
                FileOutputStream fos = null;
                ObjectOutputStream oos = null;
                oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(new Inner(PlayerMap,TriggerMap));
                oos.flush();
                oos.close();
                return true;
            }catch (Exception e2){
                getLogger().warning("[GeyserPermGroup] [VC] 兼容模式变量文件写入失败");
                e2.printStackTrace();
                return false;
            }
        }
    }

    public boolean LoadHashMapFromFile(){

        try {
            File file = new File("plugins/GeyserPermGroup/Variables.dat");
            //从文件读取
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Inner inner = null;
            try {
                inner = (Inner) ois.readObject();
                PlayerMap = inner.PlayerMap;
                TriggerMap = inner.TriggerMap;
                //关闭
                ois.close();
                return true;
            } catch (EOFException e) {
                //异常
                ois.close();
                getLogger().warning("[GeyserPermGroup] [VC] 变量文件读入失败(ReadObj)");
                e.printStackTrace();
                return false;
            }
        } catch (Exception e){
            getLogger().warning("[GeyserPermGroup] [VC] 变量文件读入失败(T)");
            e.printStackTrace();
            return false;
        }
    }

    public boolean SetPlayerHashMap (UUID PlayerUUID,HashMap<String,String> Source){
        //有异常就返回false
        try {
            PlayerMap.put(PlayerUUID,Source);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public HashMap<String,String> GetPlayerHashMap(UUID PlayerUUID){
        return (PlayerMap.get(PlayerUUID));
    }

    public void SetTrigger (String VariableName, String Target) {
        TriggerMap.put(VariableName,Target);
    }

    public String GetTrigger (String VariableName) {
        return (TriggerMap.get(VariableName));
    }

    public void RemoveTrigger (String VariableName){
        TriggerMap.remove(VariableName);
    }

    public void Trig (UUID PlayerUUID, String VariableName) {

        String Trigger = GetTrigger(VariableName);

        if (Trigger != null){

            Player player = Bukkit.getPlayer(PlayerUUID);

            //判断玩家存不存在
            if (player != null){

                //执行命令
                Bukkit.dispatchCommand(player, Trigger);
            }

        }
    }

}

class Inner implements Serializable {

    private static final long serialVersionUID = 2L;
    HashMap<UUID, HashMap<String,String>> PlayerMap;
    HashMap<String,String> TriggerMap;

    public Inner(HashMap<UUID, HashMap<String,String>> playerMap, HashMap<String,String> triggerMap) {
        this.PlayerMap = playerMap;
        this.TriggerMap = triggerMap;
    }
}
