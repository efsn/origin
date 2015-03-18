package com.esen.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.esen.util.MiniProperties;

public class ConnectFactoryManagerImpl implements ConnectFactoryManager {
  private HashMap map;
  public ConnectFactoryManagerImpl(){
    map = new HashMap();
    
  }
  public ConnectionFactory getConnectionFactory(String dsname, boolean throwIfNotExists) {
    return (ConnectionFactory)map.get(dsname);
  }
  public void setConnectionFactory(String dsname,ConnectionFactory conf){
    map.put(dsname, conf);
  }
  public String[] getConnectionFactoryNames1() {
    Set keys = map.keySet();
    String[] nms = new String[keys.size()];
    keys.toArray(nms);
    return nms;
  }

  public ConnectionFactory getDefaultConnectionFactory() {
    return (ConnectionFactory)map.get("*");
  }
  
public Connection getDefaultConnection() throws SQLException {
	return getDefaultConnectionFactory().getConnection();
}
public void addDataSource(String dsname, PoolConnectionFactory dbf) {
	// TODO Auto-generated method stub
	
}
public void close() {
	// TODO Auto-generated method stub
	
}
public void closeOtherDatasources() {
	// TODO Auto-generated method stub
	
}
public PoolConnectionFactory getDataSource(String dsname, boolean throwIfNotExists) {
	// TODO Auto-generated method stub
	return null;
}
public String[] getConnectionFactoryNames() {
	// TODO Auto-generated method stub
	return null;
}
public MiniProperties getDataSourceProperties(String dsname, boolean throwIfNotExists) {
	// TODO Auto-generated method stub
	return null;
}
public void removeDataSource(String dsname) throws SQLException {
	// TODO Auto-generated method stub
	
}
public void setDataSource(String dsname, MiniProperties props, boolean addit) {
	// TODO Auto-generated method stub
	
}
public void loadOtherDataSources(HashMap m) throws SQLException {
	// TODO Auto-generated method stub
	
}

}
