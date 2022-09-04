package dpear.gpg;

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
            oos.writeObject(new Inner(PlayerMap));
            oos.flush();
            oos.close();
            return true;
        }catch (Exception e){
            getLogger().warning("[GeyserPermGroup] [VC] 变量文件写入失败");
            e.printStackTrace();
            return false;
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
                PlayerMap = inner.hashMap;
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

}

class Inner implements Serializable {

    private static final long serialVersionUID = 2L;
    HashMap<UUID, HashMap<String,String>> hashMap;

    public Inner(HashMap<UUID, HashMap<String,String>> hashMap) {
        this.hashMap = hashMap;
    }
}
