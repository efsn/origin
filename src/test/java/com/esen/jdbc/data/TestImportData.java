package com.esen.jdbc.data;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestImportData  extends TestCase {
  public void testImportData() throws Exception{
    initDataPool();
    File f = new File("d:\\����.db");
    FileInputStream fin = new FileInputStream(f);
    try{
      DataCopy.createInstance().importData(fin, "oracle_pool", "depart", DataCopy.OPT_CREATENEWTABLE);
    }finally{
      fin.close();
    }
  }

  private void initDataPool() {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    ConnectionFactory oracle = new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:orcdb",
        "tax", "tax",true);
    fm.setConnectionFactory("oracle_pool",oracle);
    DefaultConnectionFactory.set(fm);
  }
}
