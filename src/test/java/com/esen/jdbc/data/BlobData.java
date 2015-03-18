package com.esen.jdbc.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;

import junit.framework.TestCase;

public class BlobData extends TestCase{
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:orcdb",
        "dev1", "yfone",true);
  }
  public void testBlobData() throws Exception{
    ConnectionFactory conf = createConnectionFactory();
    Dialect dl = conf.getDialect();
    DbDefiner dbf = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      String tbname = "t_blob";
      if(dbf.tableExists(conn, null, tbname))
        dbf.dropTable(conn, null, tbname);
      
      dbf.defineStringField("type_", 10, null, true, false);
      dbf.defineBlobField("data_", null, true, false);
      dbf.createTable(conn, tbname, false);
      
      PreparedStatement pstat = conn.prepareStatement("insert into "+tbname+" (type_,data_)values(?,?)");
      pstat.setString(1, "jpg");
      setData(pstat,2,"E:\\temp\\data\\Water lilies.jpg");
      pstat.executeUpdate();
      
      pstat.setString(1, "gif");
      setData(pstat,2,"E:\\temp\\data\\exclam.gif");
      pstat.executeUpdate();
      
      pstat.setString(1, "rtj");
      setData(pstat,2,"E:\\temp\\data\\LICENSE.rtf");
      pstat.executeUpdate();
      
      pstat.setString(1, "doc");
      setData(pstat,2,"E:\\temp\\data\\dim9.doc");
      pstat.executeUpdate();
      
      pstat.setString(1, "txt");
      setData(pstat,2,"E:\\temp\\data\\select into.txt");
      pstat.executeUpdate();
      
      pstat.setString(1, "bmp");
      setData(pstat,2,"E:\\temp\\data\\setup.bmp");
      pstat.executeUpdate();
      
      pstat.close();
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  private void setData(PreparedStatement pstat, int i, String path) throws Exception {
    File f = new File(path);
    FileInputStream in = new FileInputStream(f);
    try{
    pstat.setBinaryStream(i, in, -1);
    }finally{
      in.close();
    }
  }
}
