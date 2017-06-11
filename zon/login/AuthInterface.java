package zon.login;

public interface AuthInterface{
	
	/** 返回是否登录 */
	public boolean isLogged(String p);
	
	/** 返回是否注册 */
	public boolean isRegistered(String s);
	
	/** 登录, 返回boolean */
	//public boolean login(String p);
	//public boolean login(String p, byte[] password);
	public boolean login(String p, String password);
	
	/** 验证 */
	public boolean verify(String p, String password);
	
	/** 注销 */
	public boolean logout(String name);
	
	/** 注册, 返回boolean */
	public boolean register(String p, String password);
	
	/** 删除, 返回boolean */
	public boolean remove(String p);
	
	/** 改密码 */
	public boolean fix(String name, String old, String late);
}
