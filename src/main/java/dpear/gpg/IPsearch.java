package dpear.gpg;

import org.lionsoul.ip2region.xdb.Searcher;
import java.io.*;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getLogger;

public class IPsearch {

    static String dbPath = "plugins/GeyserPermGroup/ip2region.xdb"; //数据库路径
    static byte[] cBuff; //缓存文件
    static Searcher searcher; //查询对象

    public IPsearch() {

        //从 dbPath 加载整个 xdb 到内存。
        try {
            cBuff = Searcher.loadContentFromFile(dbPath);
        } catch (Exception e) {
            getLogger().warning("无法加载ip数据库,功能不可用!");
            e.printStackTrace();
            return;
        }

        //使用上述的 cBuff 创建一个完全基于内存的查询对象。
        try {
            searcher = Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            getLogger().warning("无法创建ip检索对象,功能不可用!");
            e.printStackTrace();
            return;
        }
    }

    public String search(String ip) {

        //查询
        try {
            return searcher.search(ip);
        } catch (Exception e) {
            getLogger().warning("检索ip失败!");
            e.printStackTrace();
            return "";
        }

    }

    public void close(){
        //关闭资源 - 该 searcher 对象可以安全用于并发，等整个服务关闭的时候再关闭 searcher
        if (searcher != null) {
            try {
                searcher.close();
            }catch (Exception e){
                getLogger().warning("卸载ip数据库失败,出现异常!");
                e.printStackTrace();
                return;
            }
        }else {
            getLogger().warning("卸载ip数据库失败,数据库为null!");
            return;
        }
    }

}
