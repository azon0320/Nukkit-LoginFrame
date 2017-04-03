package dor;

import cn.nukkit.event.player.*;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import java.util.LinkedHashMap;

public class Login extends PluginBase{
  public static Login instance;
  
  private LListener listener;
  
  private LinkedHashMap<String, String> datas;
  
  public static getInstance() throws NullPointerException{
    if(instance == null){
      throw new NullPointerException();
    }else{
      return instance;
    }
  }
  
  public Login(){}
  
  public void onEnable(){
    listener = new LListener(this);
    datas = (new Config(getDataFolder() + "LoginList.yml", Config.YAML, new LickedHashMap<String, String>())).getAll();
    getServer().getPluginManager().registerEvents(listener, this);
    getLogger().info("LoginFrame Loader : Complete!");
  }
  
  public void onDisable(){
    save();
  }
  
  public void save(){
      Config c = new Config(getDataFolder() + "LoginList.yml", Config.YAML, new LinkedHashMapp<String, String>());
      c.setAll(datas);
      c.save();
  }
  
  public boolean isLogined(Player p){
    return false;  
  }
}
