package com.esen.jdbc.data.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.text.NumberFormat;

import com.esen.io.MyByteArrayOutputStream;
import com.esen.jdbc.data.impl.AbstractDbDataIO;
import com.esen.util.StmFunc;

import junit.framework.TestCase;

public class AbstractDbDataIOTest extends TestCase {
  public void testReadAndWrite() throws IOException{
    AbstractDbDataIO io = new AbstractDbDataIO();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    io.saveInt(out, new Integer(2323));
    io.saveInt(out, null);
    
    io.saveLong(out,new Long(199999999999999999L));
    io.saveLong(out, null);
    
    io.saveDouble(out,new Double(2354234.3412));
    io.saveDouble(out,new Double(9999999999999999.3423));
    io.saveDouble(out,null);
    io.saveDouble(out, new Double(Double.NaN));
    
    String str = "alkdjf我的家乡在湖北；中国四川汶川大地震...";
    io.saveString(out, str);
    io.saveString(out, null);
    io.saveString(out, "");
    
    io.saveClob(out, new CharArrayReader(str.toCharArray()));
    io.saveClob(out, null);
    
    io.saveBlob(out, new ByteArrayInputStream(str.getBytes("utf-8")));
    String str2 = "alkdjf我的家乡在湖北；中国四川汶川大地震...asdf";
    io.saveBlob(out, new ByteArrayInputStream(str2.getBytes("utf-8")));
    /**
     * 测试对空窜，写入blob
     */
    String strnull = "";
    io.saveBlob(out, new ByteArrayInputStream(strnull.getBytes("utf-8")));
    /**
     * 20091121
     * 测试对blob流0长度的读取
     */
    String strnull2 = "";
    io.saveBlob(out, new ByteArrayInputStream(strnull2.getBytes("utf-8")));
    
    io.saveBlob(out, null);
    
    io.saveDate(out, Date.valueOf("2008-01-01"));
    io.saveDate(out,null);
    
    io.saveDate(out,java.sql.Time.valueOf("20:40:21"));
    io.saveDate(out, java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234"));
    
    InputStream bigin = getClass().getResourceAsStream("big.txt");
    long biglen = 0;
    try{
      biglen = io.saveBlob(out,bigin);
    }finally{
      bigin.close();
    }
    
    //用于测试用io.readBlob(in,out)读取大的流到out
    InputStream bigin2 = getClass().getResourceAsStream("big.txt");
    long biglen2 = 0;
    try{
      biglen2 = io.saveBlob(out,bigin2);
    }finally{
      bigin2.close();
    }
    
    io.saveDouble(out, new Double(210005000004723196D));
    
    
    //开始读取-------------------------------------------------------
    
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    assertEquals(2323, io.readInt(in));
    assertEquals(false,io.wasNull());
    io.readInt(in);
    assertEquals(true,io.wasNull());
    
    assertEquals(199999999999999999L,io.readLong(in));
    assertEquals(false,io.wasNull());
    io.readLong(in);
    assertEquals(true,io.wasNull());
    
    assertEquals(2354234.3412,io.readDouble(in),0);
    assertEquals(false,io.wasNull());
    assertEquals(9999999999999999.3423,io.readDouble(in),0);
    assertEquals(false,io.wasNull());
    io.readDouble(in);
    assertEquals(true,io.wasNull());
    assertEquals(true,Double.isNaN(io.readDouble(in)));
    assertEquals(false,io.wasNull());
    
    assertEquals(str,io.readString(in));
    assertEquals(false,io.wasNull());
    assertEquals(null,io.readString(in));
    assertEquals(true,io.wasNull());
    assertEquals("",io.readString(in));
    assertEquals(false,io.wasNull());
    
    assertEquals(str,io.readClob(in));
    assertEquals(null,io.readClob(in));
    assertEquals(true,io.wasNull());
    
    MyByteArrayOutputStream out2 = new MyByteArrayOutputStream();
    InputStream in2 = io.readBlob(in);
    stmTryCopyFrom(13,in2,out2);
    String str3 = new String(StmFunc.stm2bytes(out2.asInputStream()),"utf-8");
    assertEquals(str,str3);
    
    MyByteArrayOutputStream out3 = new MyByteArrayOutputStream();
    InputStream in3 = io.readBlob(in);
    stmTryCopyFrom(13,in3,out3);
    String str4 = new String(StmFunc.stm2bytes(out3.asInputStream()),"utf-8");
    assertEquals(str2,str4);
    
    MyByteArrayOutputStream outnull = new MyByteArrayOutputStream();
    InputStream innull = io.readBlob(in);
    stmTryCopyFrom(13,innull,outnull);
    String strnull3 = new String(StmFunc.stm2bytes(outnull.asInputStream()),"utf-8");
    assertEquals(strnull,strnull3);
    outnull.close();
    
    /**
     * 20091121
     * 测试读取0长度的blob
     */
    MyByteArrayOutputStream outnull2 = new MyByteArrayOutputStream();
    io.readBlob(in, outnull2);
    String strnull4 = new String(StmFunc.stm2bytes(outnull2.asInputStream()),"utf-8");
    assertEquals(strnull2,strnull4);
    outnull2.close();
    
    assertEquals(false,io.wasNull());
    assertEquals(0,io.readBlob(in,null));
    assertEquals(true,io.wasNull());
    
    assertEquals(Date.valueOf("2008-01-01"),io.readDate(in));
    assertEquals(false,io.wasNull());
    assertEquals(null,io.readDate(in));
    assertEquals(true,io.wasNull());
    
    assertEquals(java.sql.Time.valueOf("20:40:21"),io.readTime(in));
    assertEquals(false,io.wasNull());
    
    assertEquals(java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234")
        ,io.readTimestamp(in));
    assertEquals(false,io.wasNull());
    
    File f = File.createTempFile("test", "io");
    BufferedOutputStream w = new BufferedOutputStream(new FileOutputStream(f));
    try{
      InputStream in4 = io.readBlob(in);
      stmTryCopyFrom(1024*32,in4,w);
      w.flush();
      assertEquals(biglen,f.length());
    }finally{
      w.close();
      f.delete();
    }
    
    /**
     * 20091121
     * 测试用io.readBlob(in,out)读取大的流到out
     */
    File f2 = File.createTempFile("test", "io");
    BufferedOutputStream w2 = new BufferedOutputStream(new FileOutputStream(f2));
    try {
      io.readBlob(in, w2);
      assertEquals(biglen2, f2.length());
    }
    finally {
      w2.close();
      f2.delete();
    }
    
    double v = io.readDouble(in);
    //String sv = NumberFormat.getInstance().format(v);
    assertEquals(new Double(210005000004723196D).doubleValue(),v,0);
  }
  
  private int stmTryCopyFrom(int bufsize,InputStream in, OutputStream out) throws IOException{
    byte[] buf = new byte[bufsize];
    int sz = 0;
    int r;
    while ((r = in.read(buf)) != -1) {
      sz += r;
      out.write(buf, 0, r);
    }
    return sz;
  }
}
