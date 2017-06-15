package zon.login;

import cn.nukkit.Player;

/**
 * Zon Project
 *
 * 登录后触发的事件
 * 如果不给玩家任何处理，那么玩家将被传送至默认的target值地图
 */
public interface LoginListener {
    public void onLogin(Player p);
}
