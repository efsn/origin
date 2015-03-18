package com.esen.jdbc.data;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.esen.io.MyByteArrayOutputStream;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.data.impl.DataReaderFromDb;
import com.esen.jdbc.data.impl.DataReaderFromStmFactory;
import com.esen.jdbc.data.impl.DataWriterToStmNew;
import com.esen.jdbc.jdbctest.TestJdbcInit;


public class DataWriterToStmNewTest extends TestJdbcInit {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.42:1521:orcl",
        "dw", "dw",true);
  }

  public void testNewStm() throws Exception{
    createTable();
    addData(1);
    addData2();
    Connection conn = this.getConnectionFactory().getConnection();
    try{
      DataReaderFromDb dr = new DataReaderFromDb(conn,this.tablename);
      MyByteArrayOutputStream out = new MyByteArrayOutputStream();
      try{
        DataWriterToStmNew dw = new DataWriterToStmNew(out,null);
        dw.writeData(dr);
        //读
        InputStream in = out.asInputStream();
        DataReader stmrd = DataReaderFromStmFactory.getInstance().createDataReader(in);
        AbstractMetaData meta = stmrd.getMeta();
        assertEquals(3,stmrd.getRecordCount());
        while(stmrd.next()){
          for(int i=0;i<meta.getColumnCount();i++){
            System.out.print(stmrd.getValue(i)+"\t");
          }
          System.out.println();
        }
      }finally{
        out.close();
        dr.close();
      }
    }finally{
      conn.close();
    }
  }
  protected void addData2() throws Exception {
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
        + tablename
        + " (INT_,STR_,NUM_,DATE_,BINARY_)" +
            "values(?,?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      
        pstat.setLong(1, Long.MAX_VALUE);
        pstat.setString(2, "阿里斯顿开发阿斯顿");
        pstat.setDouble(3, (Math.random() * 10000) / 10);
        pstat.setDate(4, java.sql.Date.valueOf("2008-08-10"));
        pstat.setBinaryStream(5, null, 0);
        pstat.addBatch();
        
        pstat.setLong(1, Long.MAX_VALUE);
        pstat.setString(2, "阿里斯顿开发阿斯顿2");
        pstat.setDouble(3, (Math.random() * 10000) / 10);
        pstat.setDate(4, java.sql.Date.valueOf("2008-08-10"));
        InputStream in = getClass().getResourceAsStream("san.txt");
        pstat.setBinaryStream(5, in, -1);
        
        pstat.addBatch();
        
       pstat.executeBatch();
    }
    finally {
      try {
        if (pstat != null)
          pstat.close();
        if (conn != null) {
          conn.close();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
