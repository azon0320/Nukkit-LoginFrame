package zon.login;

import zon.SQLite3;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
  * Zon Project
  * author: azon
  * 2017.5.29
  *
  * Bad SQL statements in this class
  * TODO 修复SQL语句
  * 目前存在的问题: 
  * 无法记录所有数据
  */

public final class PasswordAuth implements AuthInterface{
	
	public static final int LOG_DELAY = 60*60;
	
	private SQLite3 db;
	private List<String> list;

	public PasswordAuth(SQLite3 s){
		if(s == null) throw new IllegalArgumentException();
		s.exec(
		"CREATE TABLE IF NOT EXISTS auth(" +
		"name TEXT NOT NULL," +
		"password TEXT NOT NULL" +
		");"
		);
		db = s;
		list = new ArrayList<String>();
	}
	
	public boolean isLogged(String p){
		return list.contains(p);
	}
	
	public boolean login(String name, String pass){
		if(isLogged(name)) return false;
		if(verify(name, pass)){
			list.add(name);
			return true;
		}
		return false;
	}

	
	public boolean isRegistered(String name){
		ResultSet rs = db.query(
		"SELECT password FROM auth WHERE name = '" + name + "';"
		);
		if(rs == null) return false;
		try {
			if (rs.next()) return true;
		}catch(SQLException e){}
		return false;
	}
	
	public boolean verify(String name, String pass){
		if(isRegistered(name)){
			ResultSet rs = db.query(
			"SELECT password FROM auth WHERE name = '" + name + "';"
			);
			if(rs == null) return false;
			try {
				if (!rs.next()) return false;
				if (pass.trim().equals(rs.getString("password"))) return true;
			}catch(SQLException e){}
			return false;
		}else{
			return false;
		}
	}
	
	public boolean logout(String name){
		return isLogged(name) && list.remove(name);
	}
	
	public boolean addAuth(String name, String pass){
		return isRegistered(name) && db.exec(
		"INSERT INTO auth(name, password)VALUES('" +
		name + "', '" + pass + "';"
		);
	}
	
	public boolean register(String name, String pass){
		return addAuth(name, pass);
	}
	
	public boolean remove(String name){
		if(!isRegistered(name)) return false;
		return db.exec(
		"DELETE FROM auth WHERE name = '" + name + "';"
		);
	}
	
	public boolean fix(String name, String old, String late){
		if(!verify(name, old)) return false;
		return db.exec(
		"UPDATE auth SET password = '" + late + "' WHERE name = '" + name + "';"
		);
	}
}
