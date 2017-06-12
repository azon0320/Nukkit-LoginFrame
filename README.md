# Nukkit-LoginFrame
azon

---
##介绍
> * 这是一个提供了ClientID登录接口的Nukkit插件
> * 玩家登录时将被传送至特定地图
> * 玩家登录成功后将被传送至主世界

##用法
> * 把插件安装在服务器，运行服务器
> * 关服，转至路径
> * 修改buttons.yml里面每个按钮的位置
> * 修改 Config.yml 的值后再次启动服务器

---
###指令设置按钮位置<br />
####1.按钮的数字值及config.yml属性值
```yml
<按钮种类>      <值>    <点击后执行的操作>
登录按钮        0       登录
获得公告按钮    1       发送config.yml内info的值
自定义按钮1     2       发送config.yml内invisible_egg的值
自定义按钮2     3       发送config.yml内jump_egg的值
```
##2.指令<br />
###/logloc <按钮值><br />
#####op输入后绑定当前种类的值，点击一个按钮即可获取该种类的位置<br />
#####/stop关服即可保存按钮数据<br />
##例如:<br />
##我面前有个按钮，我要将它变成"登录按钮"<br />
##我输入指令/logloc 0<br />
##然后点击面前的按钮，它就是登录用的按钮了<br />
##重启服务器，直接点击这个按钮，开始登录<br />

---

###Config,yml 属性
```yml
world       #登录所在的地图
target      #主世界所在的地图
info        #点击按钮时的公告(内部使用)
bottom_text #显示玩家屏幕上的文字

# 此处属性仅用于内部服务器
invisible_egg   
jump_egg

```
