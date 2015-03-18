package com.esen.jdbc.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;

public class MakeData {
  public static void main(String[] args) throws Exception{
    MakeData md = new MakeData();
    md.makeData();
  }
  private void makeData() throws Exception{
    createTable();
  }
  private void createTable() throws Exception {
    ConnectionFactory cf = getConnectionFactory();
    Connection conn = cf.getConnection();
    String tablename = "cfcs_xxb";
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    try {
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      DbDefiner def = cf.getDbDefiner();
      def.clearDefineInfo();
      def.defineStringField("userid_", 30, null, true, false);
      def.defineStringField("username", 30, null, true, false);
      def.defineStringField("upid_", 30, null, true, false);
      def.defineStringField("bbq_", 10, null, true, false);
      def.defineStringField("hy_dm", 30, null, true, false);
      def.defineStringField("lx_dm", 30, null, true, false);
      def.defineIntField("totalcnt", 10, null, true, false);
      def.defineFloatField("tzze", 20, 2, null, true, false);
      def.defineDateField("datebbq", null, true, false);
      def.defineIndex("Iuserbbq", "(userid_,bbq_)", false);
      def.createTable(conn, null, tablename);
      
      if(dbv.tableExists(conn,null,"dim_t_user")){
        dbv.dropTable(conn,null,"dim_t_user");
      }
      def.clearDefineInfo();
      def.defineStringField("userid_", 30, null, true, false);
      def.defineStringField("username", 30, null, true, false);
      def.defineIndex("Iuserid", "(userid_)", true);
      def.createTable(conn, null, "dim_t_user");
      
      if(dbv.tableExists(conn,null,"cfcs_b1")){
        dbv.dropTable(conn,null,"cfcs_b1");
      }
      def.clearDefineInfo();
      def.defineStringField("userid_", 30, null, true, false);
      def.defineStringField("bbq_", 30, null, true, false);
      def.defineFloatField("zb1", 20, 2, null, true, false);
      def.defineFloatField("zb2", 20, 2, null, true, false);
      def.defineFloatField("zb3", 20, 2, null, true, false);
      def.defineIndex("Iuserbbq2", "(userid_,bbq_)", false);
      def.createTable(conn, null, "cfcs_b1");
      
    }finally{
      conn.close();
      conn = null;
    }
    String[] sans = readData("san.txt");//三国人名
    conn = cf.getConnection();
    try{
      PreparedStatement pstat = conn.prepareStatement("insert into dim_t_user (userid_,username)values(?,?)");
      try{
        for(int i=0;i<sans.length;i++){
          pstat.setString(1, "P"+(i+1));
          pstat.setString(2, sans[i]);
          pstat.addBatch();
          if(i%100==0)
            pstat.executeBatch();
        }
        pstat.executeBatch();
      }finally{
        pstat.close();
      }
    }finally{
      conn.close();
      conn = null;
    }
    String[] hy_dms = readData("hy_dm.txt");//全国税收调查的行业代码
    String[] lx_dms = readData("lx_dm.txt");//全国税收调查的企业登记类型；
    String[] upids = readData("upid.txt");
    conn = cf.getConnection();
    int k=1;
    try{
      PreparedStatement pstat = conn.prepareStatement("insert into "+tablename + " (userid_,username,upid_,bbq_,hy_dm,lx_dm,totalcnt,tzze,datebbq)values(?,?,?,?,?,?,?,?,?)");
      PreparedStatement pstat2 = conn.prepareStatement("insert into cfcs_b1 (userid_,bbq_,zb1,zb2,zb3)values(?,?,?,?,?)");
      try{
        for(int i=0;i<3000;i++){
          k=1;
          int p = (int)Math.round(Math.random() * (sans.length-1));
          pstat.setString(k++, "P"+(p+1));
          pstat.setString(k++,sans[p]);
          p = (int)Math.round(Math.random() * (upids.length-1));
          pstat.setString(k++,upids[p]);
          Calendar c = getDate();
          String bbq_ = getDateStr(c);
          pstat.setString(k++, bbq_);
          p = (int)Math.round(Math.random() * (hy_dms.length-1));
          pstat.setString(k++, hy_dms[p]);
          p = (int)Math.round(Math.random() * (lx_dms.length-1));
          pstat.setString(k++, lx_dms[p]);
          pstat.setInt(k++, (int)Math.round(Math.random() * 100));
          pstat.setDouble(k++, (Math.random() * 10000) / 10);
          pstat.setDate(k++, new Date(c.getTimeInMillis()));
          pstat.addBatch();
          
          pstat2.setString(1, "P"+(p+1));
          pstat2.setString(2, bbq_);
          pstat2.setDouble(3, (Math.random() * 10000) / 10);
          pstat2.setDouble(4, (Math.random() * 10000) / 10);
          pstat2.setDouble(5, (Math.random() * 10000) / 10);
          pstat2.addBatch();
          if(i%100==0){
            pstat.executeBatch();
            pstat2.executeBatch();
          }
        }
        pstat.executeBatch();
        pstat2.executeBatch();
      }finally{
        pstat.close();
        pstat2.close();
      }
    }finally{
      conn.close();
      conn = null;
    }
  }
  private String getDateStr(Calendar c) {
    int y = c.get(Calendar.YEAR);
    int m = c.get(Calendar.MONTH)+1;
    StringBuffer bbq = new StringBuffer();
    bbq.append(y);
    if(m<10) bbq.append(0);
    bbq.append(m).append("--");
    return bbq.toString();
  }
  private Calendar getDate() {
    int p = (int)Math.round(Math.random() * 355);
    Calendar c = Calendar.getInstance();
    c.setTime(Date.valueOf("2007-01-01"));
    c.add(Calendar.DATE, p);
    return c;
  }
  private String[] readData(String filepath) throws Exception {
    //File f = new File(filepath);
    InputStream in = getClass().getResourceAsStream(filepath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    List l = new ArrayList();
    try {
      String ln = reader.readLine();
      while (ln != null) {
        l.add(ln);
        ln = reader.readLine();
      }
    }
    finally {
      reader.close();
    }
    String[] sans = new String[l.size()];
    l.toArray(sans);
    return sans;
  }
  private ConnectionFactory dbf = null;
  public ConnectionFactory getConnectionFactory(){
    if (dbf==null){
      dbf = createConnectionFactory();
    }
    return dbf;
  }
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.42:1521:orcl",
        "dw", "dw",true);
  }
}
