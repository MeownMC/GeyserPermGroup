package dpear.gpg;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class Cheru {

    public static String encrypt(String src) {
        StringBuilder stringBuilder = new StringBuilder(codes[16]);
        try {
            byte[] bytes = src.getBytes("gbk");
            for (byte b : bytes) {
                int hi = (b & 0x0000000f);
                int low = (b & 0x000000f0) >> 4;
                stringBuilder.append(codes[hi]).append(codes[low]);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String decrypt(String encrypted) {
        if(encrypted.startsWith(codes[16])) encrypted = encrypted.substring(codes[16].length());
        byte[] bytes = new byte[encrypted.length() / 2];
        for (int i = 0; i < encrypted.length(); i += 2) {
            int hi = (map.get(encrypted.substring(i + 1, i + 2))) << 4;
            int low = map.get(encrypted.substring(i, i + 1));
            bytes[i / 2] = (byte) (hi | low);
        }
        String decrypted = "";
        try {
            decrypted = new String(bytes, "gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    //length = 16+1
    private final static String[] codes = {
            "切", "卟", "叮", "咧", "哔", "唎", "啪", "啰", "啵", "嘭", "噜", "噼", "巴", "拉", "蹦", "铃",
            "切噜～♪切"
    };
    public static String getHead(){
        return codes[16];
    }
    private final static HashMap<String, Integer> map = new HashMap<>();

    static {
        for (int i = 0; i < codes.length - 1; i++) {
            map.put(codes[i], i);
        }
    }
}