package func.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import com.esen.jdbc.ConnectFactoryManager;
import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.PoolConnectionFactory;
import com.esen.util.ArrayFunc;
import com.esen.util.MiniProperties;

public class FuncConnectFactoryManager implements ConnectFactoryManager {

  private ConnectFactoryManager cfm;

  private HashMap map = new HashMap();

  public FuncConnectFactoryManager(ConnectFactoryManager cfm) {
    this.cfm = cfm;
  }

  public FuncConnectFactoryManager() {
    this(new ConnectFactoryManagerImpl());
  }

  public ConnectionFactory getConnectionFactory(String dsname, boolean throwIfNotExists) {
    ConnectionFactory fct = null;
    synchronized (map) {
      fct = (ConnectionFactory) map.get(dsname);
    }
    if (fct != null) {
      return fct;
    }
    return cfm.getConnectionFactory(dsname, throwIfNotExists);
  }

  public String[] getConnectionFactoryNames1() {
    String[] names = cfm.getConnectionFactoryNames();
    HashMap tmp = new HashMap();
    int len = names == null ? 0 : names.length;
    for (int i = 0; i < len; i++) {
      tmp.put(names[i], null);
    }
    synchronized (map) {
      names = (String[]) ArrayFunc.list2array(map.keySet());
    }
    len = names == null ? 0 : names.length;
    for (int i = 0; i < len; i++) {
      tmp.put(names[i], null);
    }
    return (String[]) ArrayFunc.list2array(tmp.keySet());
  }

  public ConnectionFactory getDefaultConnectionFactory() {
    return getConnectionFactory("*", false);
  }

  public void setConnectionFactory(String dsname, ConnectionFactory conf) {
    synchronized (map) {
      map.put(dsname, conf);
    }
  }

public Connection getDefaultConnection() throws SQLException {
	// TODO Auto-generated method stub
	return null;
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
