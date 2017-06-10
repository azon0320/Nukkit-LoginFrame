package zon.login;

import zon.SQLite3;
import zon.auth.Auth;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkkt.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;

/**
  * Zon Project
  * Zon Network内部开发代码，请勿使用
  */

public class Login extends PluginBase implements Listener{
  
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
  
  private Auth auth;
  private Level level;
  private Vector3[] buttons;
  
  public void onEnable(){
    saveDefaultConfig();
    try{
      SQLite3 sql = new SQLite3(new File(getDataFolder(), "auths.db"));
      auth = new Auth(sql);
    }catch(Exception e){
      getLogger().info("出现错误 : 数据库连接失败");
      setEnabled(false);
      return;
    }
    level = getServer().getLevelByName(getConfig().get("world"));
    if(level == null){
      getLogger().info("出现错误 : 地图加载失败");
      setEnabled(false);
      return;
    }
    buttons = new Vector3[4];
    String[] s = getConfig().get("login_button").split(",");
    buttons[LOGIN_BUTTON] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    s = getConfig().get("info_button").split(",");
    buttons[GET_INFO] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    s = getConfig().get("invisible_button").split(",");
    buttons[EGG_INVISIBLE] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    s = getConfig().get("jump_button").split(",");
    buttons[EGG_JUMP] = new Vector3(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    getServer().getPluginManager().registerEvents(this, this);
    getLogger().info("加载成功");
  }
  
  
  @EventHandler(priority = EventHandler.NORMAL, ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEVent e){
    if(auth.isLogged(e.getPlayer().getName().toLowerCase())) return;
    e.getPlayer().teleport(level.getSafeSpawn());
  }
  
  @EventHandler(priority = EventHandler.NORMAL, ignoreCancelled = true)
  public void onCmdPreprocess(PlayerPreprocessEvent e){
    if(auth.isLogged(e.getPlayer().getName().toLowerCase())) return;
    e.setCancelled(true);
  }
  
  @EventHandler(priority = EventHandler.NORMAL, ignoreCancelled = true)
  public void onPlayerClick(PlayerInteractEvent e){
    if(auth.isLogged(e.getPlayer().getName().toLowerCase())) return;
    if(e.getBlock().getID() == Block.WOODEN_BUTTON) return handle(e);
    e.setCancelled();
  }
  
  private void handle(PlayerInteractEvent e){
    int lock = 99;
    for(int i = 0; i< buttons.length; i++){
      if(e.getBlock().equals(buttons[i])){
        lock = i;
        break;
      }
    }
    switch(lock){
      case LOGIN_BUTTON:
        return doLogin(e.getPlayer());
      case GET_INFO:
        return doSendInfo(e.getPlayer(), getConfig().get("info"));
      case EGG_INVISIBLE:
        return sendTitle(e.getPlayer(), getConfig().get("invisible_egg"));
      case EGG_JUMP:
        return sendTitle(e.getlayer(), getConfig().get("jump_egg"));
      default:
        return;
    }
  }
}
