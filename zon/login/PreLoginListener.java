package zon.login;

import cn.nukkit.Player;

/**
 * Zon Project
 *
 * 需要实现方法 : onPreLogin
 * 注意!返回值很重要!!
 * 如果返回true，那么登录器将正常把玩家送入登录区进行验证!
 * 如果返回false, 那么登录器将会放弃这个玩家的验证，这个玩家只能由其他插件处理!
 * 实现时请谨慎使用返回值
 */
 
public interface PreLoginListener {
    public boolean onPreLogin(Player p);
}
