package zon.login;

import zon.SQLite3;
import zon.auth.Auth;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.level.Level;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;

public class Login extends PluginBase implements Listener{
  
  private static Login instance;
  
  public static Login getInstance(){
    return instance;
  }
  
  private Auth auth;
  private Level level;
  
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
    level = getServer().getLevelByName(getConfig().get("LoginLevel"));
    if(level == null){
      getLogger().info("出现错误 : 地图加载失败");
      setEnabled(false);
      return;
    }
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
}
