package org.svip.pool.db;

import org.svip.util.proxy.ProxyFactory;
import org.svip.util.proxy.impl.AdviceImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Blues
 * @version 1.0
 * Created on 2014/8/24
 */
public class ConnPool{

    /*
     4> when time out, auto close needless conn
     5> log
     7*> produce singleton for pool manager according to config
     8> one transaction a conn
     9>  Before exit system close all conn
     10> check conn is valid
     */
    private List<Connection> free;
    private List<Connection> active;
    private PoolParam param;


    public ConnPool(PoolParam param){
        this.param = param;
        this.init();
    }

    public synchronized Connection getConnection() throws Exception{
        if(active.size() >= param.getMaxiNum()){
            Thread.sleep(param.getWaitTime());
        }
        return this.getValidConnection(free.size() > 0 ? false : active.size() < param.getMaxiNum());
    }

    public void close(Connection con) throws SQLException{
        if(active.remove(con) && free.size() < param.getMiniNum()){
            free.add(con);
        }else{
            throw new DbPoolException("Can not remove con from active.");
        }
    }

    private Connection getValidConnection(boolean isAdd) throws SQLException{
        Connection con = null;
        if(isAdd){
            con = this.getConnectionProxy();
        }else{
            if(free.size() > 0){
                con = free.remove(0);
            }else{
                return con;
            }
        }
        if(con.isValid(param.getTimeout())){
            active.add(con);
            return con;
        }
        return this.getValidConnection(isAdd);
    }

    private void init(){
        try{
            free = new LinkedList<Connection>();
            active = new LinkedList<Connection>();
            Class.forName(param.getDriver());
            for(int i = 0; i < param.getMiniNum(); i++){
                free.add(this.getConnectionProxy());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private Connection getConnectionProxy() throws SQLException{
        Connection con = DriverManager.getConnection(param.getUrl(), param.getUsername(), param.getPassword());
        if(con != null){
            return (Connection)ProxyFactory.getProxy(con, new AdviceImpl());
        }
        return con;
    }
}
