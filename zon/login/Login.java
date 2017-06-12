package zon.login;

import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
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
  
  private static Login instance;
  
  public static Login getInstance(){
    return instance;
  }

  private PasswordAuth auth;
  private Level level;
  private Level stage;
  private Vector3[] buttons;
  private Map<String, String> tips;

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
    }, 18);
    getServer().getCommandMap().register("LevelLogin" ,new LCommand(this));
    tips = new HashMap<String, String>();
    level.addParticle(new FloatingTextParticle(buttons[0], "§b[§a登录§b]"));
    level.addParticle(new FloatingTextParticle(buttons[1], "§b[§f公告§b]"));
    currentButton = -1;
    getLogger().info("加载成功");
  }

  public void tick(){
      for(Player p : getServer().getOnlinePlayers().values()){
          if(p.isOnline()){
              try {
                  p.sendTip(tips.get(p.getName().toLowerCase()));
              }catch(Exception e){}
          }
      }
  }

  public void onDisable(){
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
  
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
  public void onPlayerJoin(PlayerJoinEvent e){
    putTip(e.getPlayer().getName().toLowerCase(), "§f如有疑问，请联系管理员\n§a点击§d各种§b按钮§f寻找§e彩蛋~");
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerSpawn(PlayerRespawnEvent e){
      if(isLogged(e.getPlayer().getName().toLowerCase())) return;
      e.setRespawnPosition(level.getSpawnLocation());
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onEntityTeleport(EntityTeleportEvent e){
      if(isLogged(e.getEntity().getName().toLowerCase()) && e.getTo().getLevel().getFolderName().equals(level.getFolderName())) e.setCancelled();
  }
  
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onCmdPreprocess(PlayerCommandPreprocessEvent e){
    if(isLogged(e.getPlayer().getName().toLowerCase())) return;
    e.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerBreakEvent(BlockBreakEvent b){
      if(!isLogged(b.getPlayer().getName().toLowerCase())){
          b.setCancelled();
      }
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerPlaceEvent(BlockPlaceEvent e){
      if(!isLogged(e.getPlayer().getName().toLowerCase())){
          e.setCancelled();
      }
  }
  
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerClick(PlayerInteractEvent e){
      if(e.getPlayer().isOp()) {
          if(e.getBlock().getId() == Block.WOODEN_BUTTON){
              if(currentButton != -1){
                  Block b = e.getBlock();
                  buttons[currentButton] = new Vector3(b.getX(), b.getY(), b.getZ());
                  currentButton = -1;
                  e.getPlayer().sendMessage("§a设置按钮成功");
              }
          }else{
              e.setCancelled();
          }
      }else {
          if (isLogged(e.getPlayer().getName().toLowerCase())) return;
          if (e.getBlock().getId() == Block.WOODEN_BUTTON) {
              handle(e);
          }
          e.setCancelled();
      }
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onPlayerPreLoginEvent(PlayerPreLoginEvent e){
        if(auth.isRegistered(e.getPlayer().getName().toLowerCase())){
            String n = e.getPlayer().getName().toLowerCase();
            if(!auth.verify(n, String.valueOf(e.getPlayer().getClientId()))){
                e.setKickMessage("§c玩家处于在线状态\n§f如有疑问，请联系管理员");
            }
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
        doLogin(e.getPlayer());
        return;
      case GET_INFO:
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
      if(auth.isRegistered(n)) {
          if (auth.verify(n, String.valueOf(p.getClientId()))) {
              p.sendTip("§a你已登录成功,传送你到" + stage.getFolderName());
              p.teleport(stage.getSpawnLocation());
          } else {
              p.kick("§cUID校验错误\n§cf如已换手机请与管理员联系");
          }
      }else{
          auth.register(n, String.valueOf(p.getClientId()));
          p.sendTip("§a已绑定您的设备ID，祝你游戏愉快\n§f如需要更换设备ID请联系管理员");
          p.teleport(stage.getSpawnLocation());
      }
  }

  private void doSendInfo(Player p, String s){
      putTip(p.getName().toLowerCase(), s);
  }
}

