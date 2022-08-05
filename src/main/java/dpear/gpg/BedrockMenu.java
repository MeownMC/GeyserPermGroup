package dpear.gpg;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.ModalForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.ModalFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Collection;
import java.util.List;

public class BedrockMenu {

    //实例化的时候获取主插件
    private final main plugin;
    private FileConfiguration config;

    public BedrockMenu(main plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void ReloadConfig(FileConfiguration config){
        this.config = config;
    }

    public Integer SendFromConfig(Player player, String name, CommandSender sender){

        //获得API
        FloodgateApi fa = FloodgateApi.getInstance();

        //获取配置菜单类型
        String type = ReadMenuData (name, "type");

        //判断菜单类型
        if (type.equals("Null")){
            sender.sendMessage("无效菜单类型");
            return -1;
        };

        //如果是ModalForm
        if (type.equals("ModalForm")){

            ModalForm.Builder MFBuilder = ModalForm.builder()
                    .title(Tools.ReplacePlaceholder(player, ReadMenuData (name, "title")))
                    .content(Tools.ReplacePlaceholder(player, ReadMenuData (name, "content")))
                    .button1(Tools.ReplacePlaceholder(player, ReadMenuData (name, "button1")))
                    .button2(Tools.ReplacePlaceholder(player, ReadMenuData (name, "button2")))
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
                            if (!ReadMenuData (name, "action.button1").equals("Null")) {
                                plugin.tools.Execute(player, ReadMenuData(name, "action.button1"));
                            }
                            return;
                        }

                        if (response.getClickedButtonId() == 1) {
                            //第二按钮
                            if (!ReadMenuData (name, "action.button2").equals("Null")) {
                                plugin.tools.Execute(player,ReadMenuData (name, "action.button2"));
                            }
                            return;
                        }

                    });

            fa.sendForm(player.getUniqueId(), MFBuilder);
            return 0;
        };

        //如果是SimpleForm
        if (type.equals("SimpleForm")){

            SimpleForm.Builder MFBuilder = SimpleForm.builder()
                    .title(Tools.ReplacePlaceholder(player, ReadMenuData (name, "title")))
                    .content(Tools.ReplacePlaceholder(player, ReadMenuData (name, "content")))
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
                            plugin.tools.Execute(player, Action.get(response.getClickedButtonId()));
                        }
                        return;

                    });

            List<String> Button = config.getStringList("Menus." + name + ".button");
            List<String> Image = config.getStringList("Menus." + name + ".image");
            for(int i=0 ; i<Button.size() ; i++) {
                if (Image.get(i).equals("Null")){
                    //不带图标的
                    MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(player, Button.get(i)));
                }else{
                    if (Image.get(i).startsWith("P~")) {
                        //本地材质
                        MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(player, Button.get(i)), FormImage.Type.PATH, Image.get(i).substring(2));
                    }else {
                        //链接图片
                        MFBuilder = MFBuilder.button(PlaceholderAPI.setPlaceholders(player, Button.get(i)), FormImage.Type.URL, Image.get(i));
                    }
                }
            }

            fa.sendForm(player.getUniqueId(), MFBuilder);
            return 0;
        };

        //如果是PlayerListForm
        if (type.equals("PlayerListForm")){

            //获得玩家列表
            Collection<? extends Player> b = Bukkit.getOnlinePlayers();
            if (ReadMenuData ( name, "removeself").equals("true")) {
                b.remove(player);
            };
            List<Player> Button = (List<Player>) b;

            SimpleForm.Builder MFBuilder = SimpleForm.builder()
                    .title(Tools.ReplacePlaceholder (player,ReadMenuData (name, "title")))
                    .content(Tools.ReplacePlaceholder (player,ReadMenuData (name, "content")))
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
                            if (!ReadMenuData( name, "buttonaction").equals("Null")) {
                                plugin.tools.Execute(player, ReadMenuData(name, "buttonaction"));
                            }
                            //选择了取消
                            return;
                        }

                        Bukkit.dispatchCommand(player, ReadMenuData ( name, "action").
                                replace("%PlayerName", Button.get(response.getClickedButtonId()).getName()).
                                replace("%PlayerUUID", Button.get(response.getClickedButtonId()).getUniqueId().toString())
                        );

                        return;

                    });

            //是否有玩家头像
            if (ReadMenuData( name, "icon").equals("true")) {
                for (Player p : Button) {
                    //生成button
                    MFBuilder = MFBuilder.button(ReadMenuData(name, "text").
                                    replace("%PlayerName", p.getName()).
                                    replace("%PlayerUUID", p.getUniqueId().toString())
                            , FormImage.Type.URL, "https://minecraft-api.com/api/skins/" + p.getName() + "/head");
                }
            }else{
                for (Player p : Button) {
                    //生成button
                    MFBuilder = MFBuilder.button(ReadMenuData(name, "text").
                            replace("%PlayerName", p.getName()).
                            replace("%PlayerUUID", p.getUniqueId().toString()));
                }
            }


            if (!ReadMenuData (name, "button").equals("Null")){
                MFBuilder = MFBuilder.button(ReadMenuData (name, "button"));
            };

            fa.sendForm(player.getUniqueId(), MFBuilder);
            return 0;


        };

        //如果是EliteMobsBossesForm
        if (type.equals("EliteMobsBossesForm")){


            SimpleForm.Builder MFBuilder = SimpleForm.builder()
                    .title(Tools.ReplacePlaceholder(player, ReadMenuData (name, "title")))
                    .content(Tools.ReplacePlaceholder(player,ReadMenuData (name, "content")))
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

                        if (plugin.elitemobs.CustomBossesName.size() == response.getClickedButtonId()){
                            if (!ReadMenuData(name, "buttonaction").equals("Null")) {
                                plugin.tools.Execute(player, ReadMenuData(name, "buttonaction"));
                            }
                            //选择了取消
                            return;
                        }

                        Bukkit.dispatchCommand(player,"em trackcustomboss " + plugin.elitemobs.CustomBossesUUID.get(response.getClickedButtonId()));

                        return;

                    });

            for (String Name : plugin.elitemobs.CustomBossesName) {
                //生成button
                MFBuilder = MFBuilder.button(Name);
            }


            if (!ReadMenuData ( name, "button").equals("Null")){
                MFBuilder = MFBuilder.button(ReadMenuData (name, "button"));
            };

            fa.sendForm(player.getUniqueId(), MFBuilder);
            return 0;


        };

        //都不匹配
        sender.sendMessage("无效菜单类型");
        return -1;
    }

    private String ReadMenuData ( String name , String paf) {
        return (config.getString("Menus." + name + "." + paf, "Null"));
    }

}
