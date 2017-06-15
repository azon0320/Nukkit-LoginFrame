package zon.login;

import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityTeleportEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.utils.Config;

import zon.SQLite3;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.File;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.command.Command;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.math.Vector3;
import cn.nukkit.level.particle.FloatingTextParticle;

/**
  * Zon Project
  * azon
  *
  * TODO Nukkit未来将不提供Config API Config需要重写
  *
  */

public class Login extends PluginBase implements Listener{

    private class LCommand extends Command{

        public Login main;

        public LCommand(Login l){
            super("logloc", "设置按钮的位置");
            main = l;
        }

        public boolean execute(CommandSender sender, String label, String[] args){
            if(sender.isOp()){
                if(args.length < 1 || !sender.isPlayer()){
                    sender.sendMessage("参数错误(/logloc [int]) 或 来自控制台的命令");
                    return false;
                }
                int a = Integer.parseInt(args[0]);
                if(a < 0 || a > 3){
                    sender.sendMessage("参数错误([登录1][公告2][隐藏彩蛋3][跳跃彩蛋4])");
                    return false;
                }
                main.currentButton = a;
                sender.sendMessage("§a绑定成功 : " + Login.getButton(a));
                return true;
            }else{
                sender.sendMessage("§c不合法的指令");
                return false;
            }
        }
    }
  
  public final int LOGIN_BUTTON = 0;
  public final int GET_INFO = 1;
  
  /** Eggs */
  public final int EGG_INVISIBLE = 2;
  public final int EGG_JUMP = 3;
  
  public final int EGG_UNKNOWN = 99;


  /** 蹲坑超一定时间踢出 */
  public final int MAX_DELAY = 2*60;
  
  private static Login instance;
  
  public static Login getInstance(){
    return instance;
  }

  private PasswordAuth auth;
  private Level level;
  private Level stage;
  private Vector3[] buttons;
  private Map<String, String> tips;
  private Map<String, Integer> times;

  /**
   * 即时按钮值
   * 未设置 : -1
   */
  private int currentButton;

  public static String getButton(int a){
      switch(a){
          case 0:
              return "登录按钮";
          case 1:
              return "公告按钮";
          case 2:
              return "隐藏按钮";
          case 3:
              return "跳跃按钮";
          default:
              return "不知从哪来的按钮";
      }
  }

  public void printError(String err){
      getLogger().notice(err);
  }

  public void onLoad(){
      instance = this;
  }
  
  public void onEnable(){
    saveDefaultConfig();
    try{
      SQLite3 sql = new SQLite3(new File(getDataFolder(), "auths.db").toString());
      auth = new PasswordAuth(sql);
    }catch(Exception e){
      getLogger().info("出现错误 : 数据库连接失败" + e.getMessage());
      setEnabled(false);
      return;
    }
    getServer().generateLevel(getConfig().getString("world"));
    getServer().generateLevel(getConfig().getString("target"));
    getServer().loadLevel(getConfig().getString("world"));
    getServer().loadLevel(getConfig().getString("target"));
    level = getServer().getLevelByName(getConfig().getString("world"));
    stage = getServer().getLevelByName(getConfig().getString("target"));
    if(level == null || stage == null){
      getLogger().info("出现错误 : 地图加载失败");
      setEnabled(false);
      return;
    }
    buttons = new Vector3[4];
      Config c = new Config(
              new File(getDataFolder(), "buttons.yml"),
              Config.YAML, new LinkedHashMap<String,Object>(){{
          put("login_button","0,0,0");
          put("info_button", "0,0,0");
          put("invisible_button", "0,0,0");
          put("jump_button", "0,0,0");
      }});
    String[] s = ((String) c.get("login_button")).split(",");
    buttons[LOGIN_BUTTON] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    s = ((String) c.get("info_button")).split(",");
    buttons[GET_INFO] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    s = ((String) c.get("invisible_button")).split(",");
    buttons[EGG_INVISIBLE] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    s = ((String) c.get("jump_button")).split(",");
    buttons[EGG_JUMP] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    getServer().getPluginManager().registerEvents(this, this);
    getServer().getScheduler().scheduleRepeatingTask(new PluginTask<Login>(this){
        public void onRun(int currentTick) {
            getOwner().tick();
        }
    }, 15);
    getServer().getCommandMap().register("LevelLogin" ,new LCommand(this));
    tips = new HashMap<>();
    times = new HashMap<String, Integer>();
    currentButton = -1;
    getLogger().info("加载成功");
  }

  /**
   * 客户端加载过程中会抛出NullPointerException
   * 每隔一段时间执行的函数故无需解决漏包问题
   * */
  public void tick(){
      for(Player p : getServer().getOnlinePlayers().values()){
          String n = p.getName().toLowerCase();
          if(isLogged(n)) continue;
          if(times.get(n) < 1){
              p.kick();
          }else {
              times.put(n, times.get(n) - 1);
              try {
                  p.sendTip(tips.get(n) + "\n§c你将在 §f" + times.get(n) + " §cs后被踢出");
              } catch (Exception e) {}
          }
      }
  }

  public void onDisable(){
      auth.close();
      if(buttons == null) return;
      Config c = new Config(
              new File(getDataFolder(), "buttons.yml"),
              Config.YAML, new LinkedHashMap<String,Object>(){{
                  put("login_button","0,0,0");
                  put("info_button", "0,0,0");
                  put("invisible_button", "0,0,0");
                  put("jump_button", "0,0,0");
      }});
      c.set("login_button", (int)buttons[0].getX() + "," + (int)buttons[0].getY() + "," + (int)buttons[0].getZ());
      c.set("info_button", (int)buttons[1].getX() + "," +(int) buttons[1].getY() + "," +(int) buttons[1].getZ());
      c.set("invisible_button", (int)buttons[2].getX() + "," + (int)buttons[2].getY() + "," +(int) buttons[2].getZ());
      c.set("jump_button", (int)buttons[3].getX() + "," + (int)buttons[3].getY() + "," +(int) buttons[3].getZ());
      c.save();
  }

  public boolean isLogged(String p){
      return !tips.containsKey(p.toLowerCase());
  }


  private void putTip(String player, String info){
      tips.put(player, "你还未登录，点击登录按钮开始验证ID" + "\n" + info);
  }


  public void deleteTip(String k){
      if(tips.get(k) != null) tips.remove(k);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerPreJoin(PlayerPreLoginEvent e){
    putTip(e.getPlayer().getName().toLowerCase(), getConfig().getString("bottom_text"));
    times.put(e.getPlayer().getName().toLowerCase(), MAX_DELAY);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
  public void onPlayerSpawn(PlayerRespawnEvent e){
      if(isLogged(e.getPlayer().getName().toLowerCase())) return;
      e.setRespawnPosition(level.getSpawnLocation());
  }

  /**隐藏后无法重新显示 , 放弃*/
  @Deprecated
  private void hidePlayer(Player p){
      for(Player pl : getServer().getOnlinePlayers().values()){
          if(pl.isOnline()){
              pl.hidePlayer(p);
          }
      }
  }

  /** 无法显示, 放弃 */
  @Deprecated
  private void showPlayer(Player p){
      for(Player pl : getServer().getOnlinePlayers().values()){
          if(pl.isOnline()){
              pl.showPlayer(p);
          }
      }
  }

  @EventHandler
  public void onPlayerJoinEven(PlayerJoinEvent e){
      level.addParticle(new FloatingTextParticle(buttons[0].add(0.5, 0.5, 1.5), "","§b[§a登录§b]"), e.getPlayer());
      level.addParticle(new FloatingTextParticle(buttons[1].add(0.5, 0.5, 1.5), "", "§b[§f公告§b]"), e.getPlayer());
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onEntityTeleport(EntityTeleportEvent e){
      if(isLogged(e.getEntity().getName().toLowerCase()) && e.getTo().getLevel().getFolderName().equals(level.getFolderName())) e.setCancelled();
  }
  
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onCmdPreprocess(PlayerCommandPreprocessEvent e){
    if(isLogged(e.getPlayer().getName().toLowerCase()) || e.getPlayer().isOp()) return;
    e.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onEntityAttack(EntityDamageEvent e){
      if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && e.getEntity().getLevel().getFolderName().equals(level.getFolderName())) e.setCancelled();
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerBreakEvent(BlockBreakEvent b){
      if(!isLogged(b.getPlayer().getName().toLowerCase())){
          b.setCancelled();
      }
  }
 
 @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 public void onPlayerChat(PlayerChatEvent e){
     if(!isLogged(e.getPlayer().getName().toLowerCase())){
         e.setCancelled();
     }
 }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerPlaceEvent(BlockPlaceEvent e){
      if(!isLogged(e.getPlayer().getName().toLowerCase())){
          e.setCancelled();
      }
  }
  
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
  public void onPlayerClick(PlayerInteractEvent e){
      /** e.getPlayer().sendMessage("§f[debug]你点击了" + e.getBlock().getId()); */
      if(isLogged(e.getPlayer().getName().toLowerCase())) return;
          if(e.getBlock().getId() == Block.WOODEN_BUTTON) {
              if (currentButton != -1 && e.getPlayer().isOp()) {
                  Block b = e.getBlock();
                  buttons[currentButton] = new Vector3(b.getX(), b.getY(), b.getZ());
                  currentButton = -1;
                  //getConfig().get("success_setting_button")
                  e.getPlayer().sendMessage("§a设置按钮成功");
              } else {
                  handle(e);
              }
          }else{
          e.setCancelled();
      }
  }
  
  private void handle(PlayerInteractEvent e){
    int lock = 99;
    for(int i = 0; i < buttons.length; i++){
      if(e.getBlock().equals(buttons[i])){
        lock = i;
        break;
      }
    }
    switch(lock){
      case LOGIN_BUTTON:
          //getConfig().getString("text_verifying")
          e.getPlayer().sendMessage("§a[Net]Verifying...");
        doLogin(e.getPlayer());
        return;
      case GET_INFO:
         /** e.getPlayer().sendMessage("§a[Net]getting Info"); */
        doSendInfo(e.getPlayer(), getConfig().getString("info"));
        return;
      case EGG_INVISIBLE:
        e.getPlayer().setSubtitle(getConfig().getString("invisible_egg"));
        return;
      case EGG_JUMP:
        e.getPlayer().setSubtitle(getConfig().getString("jump_egg"));
        return;
      default:
        return;
    }
  }

  //TODO : getClientId() is deprecated in the future, so database will be cleared!
  private void doLogin(Player p){
      String n = p.getName().toLowerCase();
      deleteTip(p.getName().toLowerCase());
      times.remove(n);
      if(auth.isRegistered(n)) {
          if (auth.verify(n, String.valueOf(p.getClientId()))) {
              //getConfig().getString("success_verify")
              //getConfig().getString("tip_success_teleport");
              p.sendMessage("§a[Net]验证成功");
              p.sendTip("§a你已登录成功,传送你到主大厅 > " + stage.getFolderName());
              //showPlayer(p);
              p.teleport(stage.getSpawnLocation());
          } else {
              //getConfig().getString("failed_verify")
              p.kick("\n§cUID校验错误\n§f如已换手机请与管理员联系");
          }
      }else{
          boolean b = auth.register(n, String.valueOf(p.getClientId()));
          if(b) {
              //getConfig().getString("success_register")
              p.sendTip("§a已绑定您的设备ID，祝你游戏愉快\n§f如需要更换设备ID请联系管理员");
          }else{
              p.sendMessage("§c设备验证失败 : SQL错误");
          }
          p.teleport(stage.getSpawnLocation());
      }
  }

  private void doSendInfo(Player p, String s){
      putTip(p.getName().toLowerCase(), s);
  }
}
