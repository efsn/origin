package com.esen.jdbc.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.data.impl.DataReaderFromCSV;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableMetaData;

import junit.framework.TestCase;

public abstract class TestDataCopyForCSV extends TestCase {
  protected  String poolname;
  private String desttable;//用于记录导入的目的表表名；
  protected void setUp() throws Exception {
    initConnectionFactory();
    super.setUp();
  }
  protected void tearDown() throws Exception {
    if(desttable!=null&&desttable.length()>0)
      deleteTable(poolname, desttable);
    deleteTable(poolname, "t_csv");
    
    super.tearDown();
  }
  
  /**
   * 测试设置导入前清空数据参数，如果写入是出现异常，原表数据是否完整；
   * @throws Exception 
   */
  public void testClearDataBeforeImport() throws Exception{
    ConnectionFactory cf = DefaultConnectionFactory.get(poolname, true);
    DbDefiner dbf = cf.getDbDefiner();
    StringBuffer csvstr = new StringBuffer(128);
    csvstr.append("1,aa\n");
    csvstr.append("2,bb\n");
    csvstr.append(",cc\n");//测试写入空串到int类型字段
    Reader csvreader = new StringReader(csvstr.toString());
    
    StringBuffer csvstr2 = new StringBuffer(128);
    /**
     * DataCopy写入数据，每10行执行一个批处理；
     * 这里前10行有需要扩容的字段，第二个字段长度只有10
     * 用于测试后面10行数据有写入异常，看在设置了“写入前清空数据”的情况下，能否回滚到清空数据前；
     */
    csvstr2.append("1,aa1\n");
    csvstr2.append("2,aa2\n");
    csvstr2.append("3,aa3\n");
    csvstr2.append("4,aa4\n");
    csvstr2.append("5,aa5\n");
    csvstr2.append("6,aa6\n");
    csvstr2.append("7,aa7\n");
    csvstr2.append("8,aa8\n");
    csvstr2.append("9,aaaaaaaaaaaaaaa\n");//需要修改字段长度
    csvstr2.append("10,aa10\n");
    
    csvstr2.append("11,aa11\n");    
    csvstr2.append("1s,aa\n");//测试写入异常
    Reader csvreader2 = new StringReader(csvstr2.toString());
    
    Connection conn = cf.getConnection();
    try{
      /**
       * 测试先删除测试表；
       */
      if(dbf.tableExists(conn, null, "T_INT"))
        dbf.dropTable(conn, null, "T_INT");
      //创建测试表；
      dbf.defineIntField("INT_", 10, null, true, false);
      dbf.defineStringField("STR_", 10, null, true, false);
      String tbname = dbf.createTable(conn, "T_INT", false);
      assertEquals(tbname,"T_INT");
      
      DataCopy.createInstance().importDataFromCSV(csvreader,conn,"T_INT",null,DataCopy.OPT_CLEARTABLE);
      
      checkImportForClearData(conn);
      
      //对t_int再次写入，设置清空表数据，但是写入时出异常，测试是否回滚，原属据是否丢失；
      try{
        DataCopy.createInstance().importDataFromCSV(csvreader2,conn,"T_INT",null,DataCopy.OPT_CLEARTABLE);
        this.assertEquals(false, true);
      }catch(Exception ex){
        this.assertEquals(true, true);
      }
      checkImportForClearData(conn);
    }finally{
      conn.close();
    }
  }
  
  private void checkImportForClearData(Connection conn) throws SQLException {
    Statement stat = conn.createStatement();
    ResultSet rs = stat.executeQuery("select INT_,STR_ from T_INT order by STR_");
    assertEquals(true, rs.next());
    this.assertEquals(1, rs.getInt(1));
    this.assertEquals("aa", rs.getString(2));
    assertEquals(true, rs.next());
    this.assertEquals(2, rs.getInt(1));
    this.assertEquals("bb", rs.getString(2));
    assertEquals(true, rs.next());
    rs.getInt(1);
    this.assertEquals(true, rs.wasNull());
    this.assertEquals("cc", rs.getString(2));
    assertEquals(false, rs.next());
    rs.close();
    stat.close();
  }
  
  /**
   * 测试导入数据时，值的长度超过字段长度，自动增加字段长度；
   * 值的长度需要根据数据库的字符集，来确定；
   * 比如：Oracle gbk字符集，中文字占一个长度； 英文字符集，占两个长度；
   * Db2  utf8字符集，占3个长度；
   * Mysql utf8,gbk字符集，占1个长度；
   * sybasease utf8 ，占3个长度；
   * sql server  gbk字符集，占2个长度；
   * @throws Exception 
   */
  public void testImportChangeFieldLength() throws Exception{
    StringBuffer csvstr = new StringBuffer(128);
    csvstr.append("1,湖北大学计算科学学院\n");
    csvstr.append("2,北京亿信华辰软件有限责任公司研发一部\n");
    Reader csvreader = new StringReader(csvstr.toString());
    String tbname = null;
    try{
      deleteTable(poolname, "t_csv");
      
      tbname = DataCopy.createInstance().importDataFromCSV(csvreader, poolname, "t_csv", null, DataCopy.OPT_CREATENEWTABLE);
      ConnectionFactory cf = DefaultConnectionFactory.get(poolname, true);
      Connection conn = cf.getConnection();
      //验证扩容字段长度；
      int collen = cf.getDialect().getTableColumnMetaData(conn, tbname, "FIELD1").getLen();
      DataBaseInfo dbinfo = cf.getDbType();
      switch(dbinfo.getNCharLength()){
        case 1:{
          assertEquals(24,collen);
          break;
        }
        case 2:{
          assertEquals(48,collen);
          break;
        }
        case 3:{
          assertEquals(72,collen);
          break;
        }
      }
      //验证数据
      try{
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select FIELD0,FIELD1 from "+tbname+" order by FIELD0");
        assertEquals(true, rs.next());
        assertEquals("湖北大学计算科学学院",rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("北京亿信华辰软件有限责任公司研发一部",rs.getString(2));
        stat.close();
        
      }finally{
        conn.close();
      }
    }finally{
      if(tbname!=null)
        deleteTable(poolname,tbname);
    }
  }
  
  /**
   * 无指定字段，第一行不是字段
   * 建新表
   * @throws Exception
   */
  public void testCSVCreateNewTable() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CREATENEWTABLE,null,"testcsv.txt");
    checkData(l);
    //检查是否创建新表；
    assertEquals(false, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 无指定字段，第一行是字段
   * 建新表
   * @throws Exception
   */
  public void testCSVCreateNewTable2() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CREATENEWTABLE,null,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    //检查是否创建新表；
    assertEquals(false, "t_csv".equalsIgnoreCase(desttable));

  }
  
  /**
   * 无指定字段，第一行不是字段
   * 覆盖目的表
   * @throws Exception
   */
  public void testCSVOverWrite() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_OVERWRITER,null,"testcsv.txt");
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23,,,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",,,,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,2007-09-12,,,",row.toString());
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 无指定字段，第一行是字段
   * 覆盖目的表
   * @throws Exception
   */
  public void testCSVOverWrite2() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CREATENEWTABLE,null,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    //检查是否创建新表；
    assertEquals(false, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 无指定字段，第一行不是字段
   * 导入前清空目的表
   * @throws Exception
   */
  public void testCSVClear() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,null,"testcsv.txt");
    checkData(l);
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 无指定字段，第一行不是字段
   * 导入前清空目的表
   * @throws Exception
   */
  public void testCSVClear2() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,null,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    //检查是否创建新表；
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 无指定字段，第一行不是字段
   * 追加记录
   * @throws Exception
   */
  public void testCSVAdd3() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",0,null,"testcsv2.txt",';','\'');
    
    assertEquals(5,l.size());
    StringBuffer row = (StringBuffer)l.get(2);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(3);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(4);
    assertEquals("',sd\"f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    //检查是否创建新表；
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  
  /**
   * 无指定字段，第一行不是字段
   * 追加记录
   * @throws Exception
   */
  public void testCSVInsert() throws Exception{
    //创建目的表，字段：STR_,STR_2,STR_3,DATE_
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",4,null,"testcsv.txt");
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数一致；
   * 建新表
   * @throws Exception
   */
  public  void testCSVHaveFields() throws Exception{
    String fields[] = new String[]{"STR1","STR2","FIELD3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CREATENEWTABLE,fields,"testcsv.txt");
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,2007-09-12,",row.toString());
    assertEquals(false, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数一致；
   * 覆盖表
   * @throws Exception
   */
  public  void testCSVHaveFields1() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_OVERWRITER,fields,"testcsv.txt");
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,2007-09-12,",row.toString());
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数一致；
   * 导入前清空目的表；给定的导入字段和目的表字段一致；
   * @throws Exception
   */
  public  void testCSVHaveFields2() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,fields,"testcsv.txt");
    checkData(l);
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数一致；
   * 导入前清空目的表；给定的导入字段和目的表字段不一致，出异常；
   * @throws Exception
   */
  public  void testCSVHaveFields22() throws Exception{
    String fields[] = new String[]{"STR1","STR2","FIELD3","DATE_"};
    createtable("t_csv",poolname);
    try{
      List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,fields,"testcsv.txt");
    }catch(Exception e){
      assertEquals(e.getMessage(),"java.lang.RuntimeException: 表t_csv不存在字段：STR1");
    }
    
  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数一致；
   * 追加记录；给定的导入字段和目的表字段一致；
   * @throws Exception
   */
  public  void testCSVHaveFields3() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_APPEND,fields,"testcsv.txt");
    
    assertEquals(5,l.size());
    StringBuffer row = (StringBuffer)l.get(2);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(3);
    assertEquals("1.23,df dfg,\"\",,",row.toString());
    
    row = (StringBuffer)l.get(4);
    assertEquals("\",sd'f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数不一致；
   * 建新表
   * @throws Exception
   */
  public  void testCSVHaveFields4() throws Exception{
    //只导入前三个字段值；
    String fields[] = new String[]{"STR1","STR2","FIELD3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CREATENEWTABLE,fields,"testcsv.txt");
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,",row.toString());
    
    assertEquals(false, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数不一致；
   * 覆盖目的表
   * @throws Exception
   */
  public  void testCSVHaveFields5() throws Exception{
    //只导入前三个字段值；
    String fields[] = new String[]{"STR1","STR2","FIELD3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_OVERWRITER,fields,"testcsv.txt");
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数不一致；
   * 导入前清空目的表, 导入字段和目的表字段一致；
   * @throws Exception
   */
  public  void testCSVHaveFields6() throws Exception{
    //只导入前三个字段值；
    String fields[] = new String[]{"STR_","STR_2","STR_3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,fields,"testcsv.txt");
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    //第4列没有导入，值为空；
    assertEquals("a,,c,,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行不是字段；导入字段和csv列数不一致；
   * 追加记录, 导入字段和目的表字段一致；
   * @throws Exception
   */
  public  void testCSVHaveFields7() throws Exception{
    //只导入前三个字段值；
    String fields[] = new String[]{"STR_","STR_2","STR_3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",0,fields,"testcsv.txt");
    
    assertEquals(5,l.size());
    StringBuffer row = (StringBuffer)l.get(2);
    assertEquals("a,,c,,",row.toString());
      
    row = (StringBuffer)l.get(3);
    assertEquals("1.23,df dfg,\"\",,",row.toString());
    
    row = (StringBuffer)l.get(4);
    assertEquals("\",sd'f,,sd\n12,,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));

  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数一致；
   * 建新表,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields8() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CREATENEWTABLE,fields,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,2007-09-12,",row.toString());
    
    assertEquals(false, "t_csv".equalsIgnoreCase(desttable));
    
  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数一致；
   * 覆盖表,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields9() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_OVERWRITER,fields,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,2007-09-12,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));
  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数一致；
   * 导入前清空,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields10() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,fields,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));
    
  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数一致；
   * 追加记录,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields11() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3","DATE_"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",0,fields,"testcsv2.txt",';','\'');
    
    assertEquals(5,l.size());
    StringBuffer row = (StringBuffer)l.get(2);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(3);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(4);
    assertEquals("',sd\"f,,sd\n12,2007-09-12 00:00:00,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));
    
  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数不一致；
   * 追加记录,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields12() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",0,fields,"testcsv2.txt",';','\'');
    
    assertEquals(5,l.size());
    StringBuffer row = (StringBuffer)l.get(2);
    assertEquals("a,,c,,",row.toString());
      
    row = (StringBuffer)l.get(3);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(4);
    assertEquals("',sd\"f,,sd\n12,,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));
    
  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数不一致；
   * 建新表,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields13() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CREATENEWTABLE,fields,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    //创建新表只有3个字段值；
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,",row.toString());
    
    assertEquals(false, "t_csv".equalsIgnoreCase(desttable));
    
  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数不一致；
   * 覆盖表,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields14() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_OVERWRITER,fields,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));
  }
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数不一致；
   * 导入前清空,导入字段是与csv第一行字段值一致；
   * @throws Exception
   */
  public  void testCSVHaveFields15() throws Exception{
    String fields[] = new String[]{"STR_","STR_2","STR_3"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,fields,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));
    
  }
  
  /**
   * 指定导入字段，csv第一行是字段；导入字段和csv列数不一致；
   * 导入前清空,导入字段是与csv第一行字段值一致，字段顺序和csv不一致
   * @throws Exception
   */
  public  void testCSVHaveFields16() throws Exception{
    String fields[] = new String[]{"STR_","STR_3","STR_2"};
    createtable("t_csv",poolname);
    List l = testCSV(poolname,"t_csv",DataCopy.OPT_CLEARTABLE,fields,"testcsv2.txt",';','\'');
    
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,'',,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("',sd\"f,,sd\n12,,",row.toString());
    
    assertEquals(true, "t_csv".equalsIgnoreCase(desttable));
    
  }
  
  
  private List testCSV(String pool,String tbname,int opt,String fields[],String filepath) throws Exception{
    return testCSV(pool,tbname,opt,fields,filepath,',','"');
  }
  private List testCSV(String pool,String tbname,int opt,String fields[],String filepath,char separator, char quote) throws Exception{
    InputStream in = getClass().getResourceAsStream(filepath);
    try{
      desttable = DataCopy.createInstance().importDataFromCSV(new InputStreamReader(in), pool, tbname, fields, opt,0,separator,quote);
    }finally{
      in.close();
    }
    File tmf = File.createTempFile("t001", "csv");
    FileWriter out = new FileWriter(tmf);
    
    try{
      DataCopy.createInstance().exportDataToCSV(pool, desttable,out);
    }finally{
      out.close();
    }
    InputStream tin = new FileInputStream(tmf);
    InputStreamReader r = new InputStreamReader(tin);
    DataReaderFromCSV dr = new DataReaderFromCSV(r);
    List l = new ArrayList();
    try{
      while(dr.next()){
        StringBuffer row = new StringBuffer();
        String[] line = dr.getLineValues();
        for(int i=0;i<line.length;i++){
          //System.out.print(dr.getValue(i)+",");
          Object o = dr.getValue(i);
          if(o instanceof Date){
            Date od = (Date)o;
            row.append(od.toString()+",");
          }else{
            row.append(o.toString()+",");
          }
        }
        l.add(row);
       // System.out.println();
      }
    }finally{
      dr.close();
      tin.close();
      tmf.delete();
    }
    checkTable(pool,desttable,fields);
    
    return l;
    
  }
  /**
   * 检查字段；
   * @param pool
   * @param tbname
   * @param fields
   * @throws Exception 
   */
  private void checkTable(String pool, String tbname, String[] fields) throws Exception {
    if(fields==null) return;
    ConnectionFactory cf = DefaultConnectionFactory.get(pool, true);
    TableMetaData tmd = cf.getDialect().createDbMetaData().getTableMetaData(tbname);
    //每个指定的字段名都应该是目的表的字段；
    for(int i=0;i<fields.length;i++){
      assertEquals(true,isfield(fields[i],tmd));
    }
  }

  private boolean isfield(String fd, TableMetaData tmd) {
    for(int i=0;i<tmd.getColumnCount();i++){
      if(tmd.getColumnName(i).equalsIgnoreCase(fd))
        return true;
    }
    return false;
  }
  private void checkData(List l){
    assertEquals(4,l.size());
    StringBuffer row = (StringBuffer)l.get(1);
    assertEquals("a,,c,2002-01-23 00:00:00,",row.toString());
      
    row = (StringBuffer)l.get(2);
    assertEquals("1.23,df dfg,\"\",,",row.toString());
    
    row = (StringBuffer)l.get(3);
    assertEquals("\",sd'f,,sd\n12,2007-09-12 00:00:00,",row.toString());
  }
  private void deleteTable(String pool, String tbname) throws Exception, SQLException {
    ConnectionFactory cf = DefaultConnectionFactory.get(pool, true);
    DbDefiner dbv = cf.getDbDefiner();
    Connection conn = null;
    try{
      conn =cf.getConnection();
      if(dbv.tableExists(conn,null,tbname)){
        dbv.dropTable(conn,null,tbname);
      }
      
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  
  private void createtable(String tbname,String pool) throws Exception {
    ConnectionFactory cf = DefaultConnectionFactory.get(pool, true);
    DbDefiner dbv = cf.getDbDefiner();
    Connection conn = null;
    try{
      conn =cf.getConnection();
      if(dbv.tableExists(conn,null,tbname)){
        dbv.dropTable(conn,null,tbname);
      }
      dbv.defineStringField("STR_",  50, null, true, false);
      dbv.defineStringField("STR_2",  50, null, true, false);
      dbv.defineStringField("STR_3",  50, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.createTable(conn,null,tbname);
      
      //插入一条记录
      PreparedStatement pstat = conn.prepareStatement("insert into "+tbname+" (STR_,STR_2,STR_3,DATE_)values(?,?,?,?)");
      pstat.setString(1, "aa");
      pstat.setString(2, "bb");
      pstat.setString(3, "cc");
      pstat.setDate(4, new Date(System.currentTimeMillis()));
      pstat.addBatch();
      pstat.executeBatch();
      pstat.close();
    }finally{
      if(conn!=null)
        conn.close();
    }
    
  }
  protected abstract void initConnectionFactory();
    
}
