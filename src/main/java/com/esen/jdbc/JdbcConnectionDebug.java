package com.esen.jdbc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.util.MiniProperties;
import com.esen.util.StrFunc;

/**
 * 用来监控连接的释放与否
 * <p>Copyright (c) 2011</p>
 * <p>北京亿信华辰软件有限责任公司</p>
 * @author jzp
 * @createdate Jun 7, 2011
 */
public class JdbcConnectionDebug {

  private HashSet pools = new HashSet(); 
  
  public JdbcConnectionDebug() {
  	
  }
  
  /**
   * 获得链接池路径放入池中
   * @param conn
   */
  public synchronized void put(Connection conn) {
    pools.add(conn);
  }
  
  /**
   * 移除连接
   * @param conn
   */
  public synchronized void remove(Connection conn){
    pools.remove(conn);
  }
  
  /**
   * 获得计数
   */
  public synchronized int getCount(){
    return pools.size();
  }
  /**
   * 打印系统目前正在使用的连接
   * @return
   */
  public synchronized void printPool(Writer out){
    try {
      Iterator it = pools.iterator();
      while(it.hasNext()){
        Object next = it.next();
        if( next==null ) continue;
        out.write("------------------CONNECTION ID "+next.hashCode()+"------------------");
        out.write(System.getProperty("line.separator"));
        String e = ((PooledConnection) next).getCallStackTrace();
        out.write(e);
        out.write(System.getProperty("line.separator"));
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
  
  /**
   * 获取正在使用的连接相关信息
   * @return List<MiniProperties>
   */
  public synchronized List<MiniProperties> getPools(){
    List<MiniProperties> list = new ArrayList<MiniProperties>();
    Iterator it = pools.iterator();
    while(it.hasNext()){
      PooledConnection next = (PooledConnection) it.next();
      if( next==null ) continue;
      MiniProperties pro = new MiniProperties();
      pro.put("stack", next.getCallStackTrace());
      pro.put("createtime", StrFunc.formatDateTime(next.getCreateTime()));
      pro.put("lasttime", StrFunc.formatDateTime(next.getLastGetTime()));
      pro.put("hashcode", next.hashCode());
      list.add(pro);
    }
    return list;
  }
  
  /**
   * 连接池堆栈信息打印到文件中．rootdir/conf/connpools.log;
   */
  public synchronized void save2File(String file){
    FileWriter w = null;
    try {
      w = new FileWriter(file);
      printPool(w);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }finally{
      if( w!=null )
        try {
          w.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    }
    
  }
  
  public final void clear(){
    pools.clear();
  }
}
