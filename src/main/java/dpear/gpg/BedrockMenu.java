package dpear.gpg;

import com.gmail.olexorus.themis.A;
import com.gmail.olexorus.themis.E;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.ModalForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.component.DropdownComponent;
import org.geysermc.cumulus.component.SliderComponent;
import org.geysermc.cumulus.component.StepSliderComponent;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.ModalFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class BedrockMenu {

    //实例化的时候获取主插件
    private final main plugin;
    private FileConfiguration config;

    //生成缓存
    private ArrayList<ModalForm.Builder> MFCache = new ArrayList<ModalForm.Builder>();
    private ArrayList<SimpleForm.Builder> SFCache = new ArrayList<SimpleForm.Builder>();
    private ArrayList<CustomForm.Builder> CFCache = new ArrayList<CustomForm.Builder>();


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

        //如果是CustomForm
        if (type.equals("CustomForm")) {

            CustomForm.Builder CFBuilder = CustomForm.builder()
                    .title(Tools.ReplacePlaceholder(player, ReadMenuData (name, "title")));


            //添加控件
            List<String> Components = config.getStringList("Menus." + name + ".Form");
            for (String component:Components) {
                String Stype = ReadMenuData (name, "Components." + component + ".type");
                    if (Stype.equals( "Dropdown" )) {

                        //获取标题
                        DropdownComponent.Builder DCBuilder = DropdownComponent.builder()
                                .text(Tools.ReplacePlaceholder(player, ReadMenuData(name, "Components." + component + ".text")));

                        //获取选项列表
                        List<String> Options = config.getStringList("Menus." + name + ".Components." + component + ".options");

                        //简单生成
                        for (String Option : Options) {
                            DCBuilder.option(Option);
                        }

                        //加入
                        CFBuilder.dropdown(DCBuilder);
                    }

                if (Stype.equals( "Input" )) {

                    //生成输入
                    CFBuilder.input(
                            Tools.ReplacePlaceholder(player, ReadMenuData(name, "Components." + component + ".title")),
                            Tools.ReplacePlaceholder(player, ReadMenuData(name, "Components." + component + ".content")));

                }
                if (Stype.equals( "Label" )) {

                    CFBuilder.label(Tools.ReplacePlaceholder(player, ReadMenuData(name, "Components." + component + ".text")));
                }
                if (Stype.equals( "Slider" )) {

                    CFBuilder.slider(
                            Tools.ReplacePlaceholder(player, ReadMenuData(name, "Components." + component + ".title")),
                            Float.parseFloat(ReadMenuData(name, "Components." + component + ".left")),
                            Float.parseFloat(ReadMenuData(name, "Components." + component + ".right")),
                            config.getInt("Menus." + name + ".Components." + component + ".step"),
                            Float.parseFloat(ReadMenuData(name, "Components." + component + ".default")));
                }
                if (Stype.equals( "StepSlide" )) {

                    //获取标题
                    StepSliderComponent.Builder SSBuilder = StepSliderComponent.builder()
                            .text(Tools.ReplacePlaceholder(player, ReadMenuData(name, "Components." + component + ".text")));

                    //获取选项列表
                    List<String> Steps = config.getStringList("Menus." + name + ".Components." + component + ".steps");

                    //简单生成
                    for (String Step : Steps) {
                        SSBuilder.step(Step);
                    }

                    //默认step
                    SSBuilder.defaultStep(config.getInt("Menus." + name + ".Components." + component + ".default"));

                    //加入
                    CFBuilder.stepSlider(SSBuilder);

                }
                if (Stype.equals( "Toggle" )) {

                    CFBuilder.toggle(
                            Tools.ReplacePlaceholder(player, ReadMenuData(name, "Components." + component + ".title")),
                            config.getBoolean("Menus." + name + ".Components." + component + ".default"));
                }
            }

            //处理头
            CFBuilder.responseHandler((form, responseData) -> {
                        CustomFormResponse response = form.parseResponse(responseData);

                        //获取列表
                        List<String> target = config.getStringList("Menus." + name + ".Target");

                        //获取命令列表
                        for (String command:target) {

                            //处理命令
                            int LastIndex = 0;

                            while (true) {
                                int StartIndex = command.indexOf("{Placeholder:", LastIndex);

                                if (StartIndex == -1) {
                                    break;
                                }

                                int EndIndex = command.indexOf("}", StartIndex);

                                String RePlaceholder = command.substring(StartIndex + 13, EndIndex);

                                if (RePlaceholder.startsWith("C~")) {

                                    //重新匹配
                                    RePlaceholder = RePlaceholder.substring(2);

                                    int CNumber;
                                    //防止有人不输入数字
                                    try {
                                        CNumber = Integer.parseInt(RePlaceholder);
                                    } catch (Exception e) {
                                        CNumber = 0;
                                        getLogger().warning("[GeyserPermGroup] [BedrockMenu] Target错误");
                                    }

                                    String CType = ReadMenuData(name, "Components." + Components.get(CNumber) + ".type");


                                    if (CType.equals("Dropdown")) {
                                        command = command.substring(0, StartIndex) +
                                                config.getStringList("Menus." + name + ".Components." + Components.get(CNumber) + ".options").get(response.getDropdown(CNumber)) +
                                                command.substring(EndIndex + 1);

                                    }

                                    if (CType.equals("Input")) {
                                        command = command.substring(0, StartIndex) +
                                                response.getInput(CNumber) +
                                                command.substring(EndIndex + 1);

                                    }

                                    if (CType.equals("StepSlide")) {
                                        command = command.substring(0, StartIndex) +
                                                config.getStringList("Menus." + name + ".Components." + Components.get(CNumber) + ".steps").get(response.getStepSlide(CNumber)) +
                                                command.substring(EndIndex + 1);

                                    }

                                } else {


                                    int CNumber;
                                    //防止有人不输入数字
                                    try {
                                        CNumber = Integer.parseInt(RePlaceholder);
                                    } catch (Exception e) {
                                        CNumber = 0;
                                        getLogger().warning("[GeyserPermGroup] [BedrockMenu] Target错误");
                                    }

                                    String CType = ReadMenuData(name, "Components." + Components.get(CNumber) + ".type");

                                    if (CType.equals("Dropdown")) {
                                        command = command.substring(0, StartIndex) +
                                                response.getDropdown(CNumber) +
                                                command.substring(EndIndex + 1);

                                    }

                                    if (CType.equals("Input")) {
                                        command = command.substring(0, StartIndex) +
                                                response.getInput(CNumber) +
                                                command.substring(EndIndex + 1);

                                    }

                                    if (CType.equals("Slider")) {
                                        command = command.substring(0, StartIndex) +
                                                response.getSlider(CNumber) +
                                                command.substring(EndIndex + 1);

                                    }

                                    if (CType.equals("StepSlide")) {
                                        command = command.substring(0, StartIndex) +
                                                response.getStepSlide(CNumber) +
                                                command.substring(EndIndex + 1);

                                    }

                                    if (CType.equals("Toggle")) {
                                        command = command.substring(0, StartIndex) +
                                                response.getToggle(CNumber) +
                                                command.substring(EndIndex + 1);

                                    }

                                    //跳出或累加
                                    LastIndex = StartIndex;
                                }
                            }

                            //执行命令
                            plugin.tools.Execute(player,command);

                        }

                    });

            fa.sendForm(player.getUniqueId(), CFBuilder);
            return 0;

        }

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

    public Integer SendFromCache(Player player, int subscript, CommandSender sender){
        if (subscript < 0){
            //非法缓存位置
            return -1;
        }

        //拆分数据
        int Type = subscript % 3;
        int FinalSubscript = subscript / 3;

        //加载缓存并发送
        try {

            if (Type == 0){
                FloodgateApi.getInstance().sendForm(player.getUniqueId(),MFCache.get(FinalSubscript));
            }
            if (Type == 1){
                FloodgateApi.getInstance().sendForm(player.getUniqueId(),SFCache.get(FinalSubscript));
            }
            if (Type == 2){
                FloodgateApi.getInstance().sendForm(player.getUniqueId(),CFCache.get(FinalSubscript));
            }

        //懒得判断下标了,这样写一起捕获异常
        }catch (IndexOutOfBoundsException e){
            getLogger().warning("[BedrockMenu] 请求了不存在的缓存" + subscript + "[" + Type + "," + FinalSubscript +" ]");
            e.printStackTrace();
            return -2;
        }catch (Exception oe){
            getLogger().warning("[BedrockMenu] 读取缓存时出现错误");
            oe.printStackTrace();
            return -3;
        }
        return 0;
    }

    public Integer SaveCache_ModalForm(ModalForm.Builder builder){
        MFCache.add(builder);
        return MFCache.size() * 3;
    }

    public Integer SaveCache_SimpleForm(SimpleForm.Builder builder){
        SFCache.add(builder);
        return (SFCache.size() * 3) + 1;
    }

    public Integer SaveCache_CustomForm(CustomForm.Builder builder){
        CFCache.add(builder);
        return (CFCache.size() * 3) + 2;
    }

    private String ReadMenuData ( String name , String paf) {
        return (config.getString("Menus." + name + "." + paf, "Null"));
    }

}
