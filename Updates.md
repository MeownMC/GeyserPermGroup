#<font color=#FF8247 >Update log</font><br>
##Version:1.3 and earlier:
<font color=BBBBDD>I didn't write update log that time, and I forget what did I do each version.<br>
So no update log here :(</font><br>
##Version:1.4:
1.修改了给玩家发送的文本的方式，变为配置文件数组<br>
2.配置文件大改<br>
3.修改了自动修改密码和注册部分的代码<br>
##Version:1.5:
1.修改了执行命令的方式，变为配置文件数组<br>
2.使名字前带点的JE玩家踢出消息可自定义<br>
3.让注册命令和改密码命令不再硬编码，改为配置文件可调<br>
4.添加了执行命令时判断是否为null<br>
##Version:1.6:
1.合并了自动注册和自动修改密码的配置到OnBedrockPlayerJoin和OnBedrockPlayerRegister里<br>
2.使Login命令可以自定义<br>
3.测试PlayerJoinEvent里面加Timer，目前还不可用！<br>
##Version:1.7:
1.添加了给菜单功能<br>
2.修改了BE玩家判断机制，使用了floodgateapi<br>
##Version:1.8:
1.添加了btpa菜单<br>
2.添加了权限管理机制<br>
>使用bemenu命令: dpear.gpg.menu<br>
>重载: dpear.gpg.menu.reload<br>
>为自己打开菜单: dpear.gpg.menu.open.self<br>
>为别人打开菜单: dpear.gpg.menu.open.allplayer<br>
>打开指定菜单: dpear.gpg.menu.菜单名称<br>

3.又移除了btpa菜单，合并到主菜单里面了<br>
4.添加了PlayerListForm菜单<br>
##Version:1.9:
1.可以使用PAPI变量了<br>
2.添加了调试功能<br>

>bemenu admin:获得op<br>
>bemenu dcmd 命令:以控制台执行命令<br>

3.添加了bemenu about命令，显示关于<br>
4.添加了bemenu plreload命令，调用PlugMan重载整个插件<br>
5.在PlayerListForm中添加了removeself[STILL DEV]<br>
##Version:2.0:
1.调用了AuthMe API，可以直接从内部自动登录了<br>
2.修改了命令，改为了/bemenu open [PlayerName] [MenuName]<br>
3.添加了防止在控制输入bemenu open @s *的部分<br>
4.添加了帮助页面/bemenu help<br>
5.支持了TAB补全<br>
6.调用了AuthMe API，可以检测玩家是否注册并且执行对应部分<br>
7.添加了命令简写功能<br>
支持Soft和Hard模式<br>
Soft:修改玩家执行的命令[不支持补全和命令白名单]<br>
Hard:处理玩家执行该命令的事件，并独立以玩家身份执行命令，支持参数转换[支持补全和命令白名单]<br>
8.添加了AuthMe和PlugMan进soft-depend<br>
9.执行/bemenu plreload时会检查有没有PlugMan了<br>
##Version:2.1:
1.支持按照不同的MC版本执行不同命令和发送不同消息<br>
2.添加了机器码验证<br>
3.添加了Vault支持，可以直接修改权限组<br>
4.添加了命令/bemenu authmelogin 玩家名 可以强制登入玩家<br>
5.添加了PAPI变量支持<br>

>%geyserpermgroup_version% 玩家版本<br>
>%geyserpermgroup_isBedrock% 是否是基岩版玩家<br>

6.让帮助信息和指令列表更好看<br>
7.修改了关于本插件信息<br>
8.添加了命令/bemenu listversion 可以列出所有玩家的版本<br>
9.添加了命令补全时候播放声音*<br>
10.修改了指令转接的格式，功能更多(你的下一个指令转接，何必只是一个指令转接)<br>
> 以控制台身份执行: Console~<br>
> 发送消息: Msg~<br>

11.将给基岩版玩家发送菜单独立成方法，优化代码可读性<br>
12.添加了钢琴功能，指令：<br>
>/bemenu piano [KEY]

键位：<br>
![PIANO KEY](https://i-s2.328888.xyz/2022/06/27/62b958c2245a2.png)
<b>(p.s. 小写是中音   大写是高音   小写前面加/是低音)<b><br>

送两首歌：<br>
###Ode an die Freude
对照表：
>33455432<br>
><b>HHJKKJHG</b><br>
>1123322<br>
><b>FFGHHGG</b><br>
>33455432<br>
><b>HHJKKJHG</b><br>
>1123211<br>
><b>FFGHGFF</b><br>
>22312343<br>
><b>GGHFGHJHF</b><br>
>2343212₅<br>
><b>GHJHGFGA</b><br>
>33455432<br>
><b>HHJKKJHG</b><br>
>1123211<br>
><b>FFGHGFF</b><br>

纯享版：
>HHJKKJHGFFGHHGGHHJKKJHGFFGHGFFGGHFGHJHFGHJHGFGAHHJKKJHGFFGHGFF

###Flower dance:
纯享版：
>HGLGHGSG
>HGLGHGSG
>HGLGHGLGM

(有隐藏的弹射功能哦~)<br>

13.基岩版PlayerListForm添加显示玩家头像选项[仅支持Java正版玩家]<br>
14.现在命令转接可以支持Tab补全了[大概吧]<br>
15.添加了切噜语翻译功能(玩家输入 "切噜～♪切" 前缀自动翻译)
>加密:/bemenu cheru encrypt 内容<br>
>解密:/bemenu cheru decrypt 内容<br>

(聊天栏输入ToCheRu前缀的在使用TrChat等聊天插件时不起作用)<br>
16.开始制作网页接口<br>
17.添加了一键放置光源的功能<br>
>/bemenu light [clear/亮度]

18.增加了动态模拟距离和调整视距的功能<br>
19.支持根据世界设置不同视距<br>
20.添加了修复rs的PAPI变量<br>
21.基岩版菜单可以引用本地图片了，使用P~开头<br>
22.添加了注册前人机验证<br>
23.为自动登入添加了在AuthMe环境下可使用指令注册<br>
24.添加了精英怪CustomBosses追踪菜单<br>