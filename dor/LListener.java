package dor;

import cn.nukkit.event.player.*;
import cn.nukkit.event.*;

public class LListener implements Listener{

  Login main;
  
  public LListener(Login m){
    main = m;
  }
  
  private Login getMain(){
    return main;
  }
  
  @EventListener(priority = EventPriority.LOWEST, ignoreCancelled = false)
  public void onPlayerMoveEvent(PlayerMoveEvent e){
    if(getMain().isLogined(e.getPlayer())) return;
    e.setCancelled();
  }
}
