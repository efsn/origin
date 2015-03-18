package com.esen.jdbc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import com.esen.io.MyByteArrayOutputStream;
import com.esen.util.ExceptionHandler;
import com.esen.zip.DeflaterOutputStream;

import junit.framework.TestCase;

public class TestStrTest extends TestCase {
  public void test1()throws Exception{
    PrintWriter bufwriter;
    PrintWriter gzipwriter;
    PrintWriter deflatewriter;
    MyByteArrayOutputStream bufstm;
    GZIPOutputStream gzipstm = null;
    DeflaterOutputStream deflatestm;
    OutputStream stm;//当前使用的stm
    PrintWriter writer;//当前使用的writer    
    bufstm = new MyByteArrayOutputStream(1024*2);
    try {
      gzipstm = new GZIPOutputStream(bufstm);
    }
    catch (IOException ex) {
      ExceptionHandler.rethrowRuntimeException(ex);
    }
    //deflatestm = new DeflaterOutputStream(bufstm);
    //bufwriter = new ClientResult_PrintWriter(new OutputStreamWriter(bufstm));
    gzipwriter = new PrintWriter(gzipstm);
    //deflatewriter = new ClientResult_PrintWriter(new OutputStreamWriter(deflatestm));
    
    gzipwriter.println("aslkdf;lasdjf;lkajsd;lfkjas;ljfdlkasdfsadkjfkaskdfkjakjsdfjasdfsdfghs");
    gzipwriter.flush();
    
    gzipstm.flush();
    gzipstm.finish();
    this.assertTrue(bufstm.size()>10);
  }
  public void testNum(){
    String maxlong = NumberFormat.getInstance().format(Long.MAX_VALUE);
    double dd = 210005000004723196D;
    Double d = new Double(dd);
    
    String v = NumberFormat.getInstance().format(d);
    Long l = new Long(210005000004723196L);
    String v2 = NumberFormat.getInstance().format(l);
    
    BigDecimal bd = new BigDecimal(210005000004723196D);
    String v3 = NumberFormat.getInstance().format(bd.doubleValue());
    
    
    String v4 = NumberFormat.getInstance().format(dd);
  }
  
  public void testSelect(){
    assertEquals("AtBname", getTablename("select * from AtBname "));
    assertEquals("AtBname", getTablename("select * from AtBname"));
    assertEquals("AtBname7", getTablename(" seLect  *  froM  AtBname7 where a>0"));
  }
  private String getTablename(String sql){
    Pattern p = Pattern.compile("\\s*select\\s+\\*\\s+from\\s+([a-z]+[a-z_0-9$]*)\\s*.*",Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(sql);
    if(m.find()){
      return m.group(1);
    }
    return null;
  }
  
  public void test3(){
    matchTableAlie("Ab$_.SWJG_$DM","Ab$_");
    matchTableAlie("SUBSTR(Ab$_.SWJG_$DM,1,5)","Ab$_");
    matchTableAlie("ab2.SWJG_$DM","ab2");
    matchTableAlie("ab2$_.SWJG_$DM","ab2$_");
    matchTableAlie("SUBSTR(ab2_.SWJG_$DM,1,5)","ab2_");
  }
  private void matchTableAlie(String gstr,String alie) {
    Pattern pt = Pattern.compile("(.*[^\\w\\$]{1})?([\\w\\S]+)\\.([\\w\\$]+).*",Pattern.CASE_INSENSITIVE);
    Matcher m = pt.matcher(gstr);
    assertEquals(true,m.matches());
    String gstr1 = m.group(2);
    assertEquals(gstr1,alie);
  }
  
	public void test4() {
		Pattern pt = Pattern.compile("(.*[^desc\\s*|asc\\s*])\\s*(\\s+desc\\s*|\\s+asc\\s*)?", Pattern.CASE_INSENSITIVE);
		Matcher m = pt.matcher("A1+B1 desc   ");
		assertEquals(true, m.matches());
		String gstr1 = m.group(1);
		assertEquals(gstr1, "A1+B1");
		String gstr2 = m.group(2);
		assertEquals(gstr2, " desc   ");
		
		m = pt.matcher("A1  Asc");
		assertEquals(true, m.matches());
		gstr1 = m.group(1);
		assertEquals(gstr1, "A1");
		gstr2 = m.group(2);
		assertEquals(gstr2, " Asc");
		
		m = pt.matcher("A1");
		assertEquals(true, m.matches());
		gstr1 = m.group(1);
		assertEquals(gstr1, "A1");
		gstr2 = m.group(2);
		assertEquals(gstr2, null);
		
		m = pt.matcher("A1+C1  ");
		assertEquals(true, m.matches());
		gstr1 = m.group(1);
		assertEquals(gstr1, "A1+C1");
		gstr2 = m.group(2);
		assertEquals(gstr2, null);
		
		m = pt.matcher("A1desc");
		assertEquals(false, m.matches());
		m = pt.matcher("A1asc");
		assertEquals(false, m.matches());
		
	}
	
	public void testCode(){
		assertEquals(true,"0000000".matches("0*"));
		assertEquals(false,"1000000".matches("0*"));
		assertEquals(false,"0100000".matches("0*"));
		
		assertEquals(true,"1000000".matches("[1,2]0*"));
		assertEquals(true,"2000000".matches("[1,2]0*"));
		assertEquals(false,"3000000".matches("[1,2]0*"));
		assertEquals(false,"1100000".matches("[1,2]0*"));
		assertEquals(false,"1110000".matches("[1,2]0*"));
		
		assertEquals(true,"1010000".matches("[1,2]\\d*[1-9]0{4,5}"));
		assertEquals(false,"1010100".matches("[1,2]\\d*[1-9]0{4,5}"));
		assertEquals(false,"1000000".matches("[1,2]\\d*[1-9]0{4,5}"));
		assertEquals(true,"1100000".matches("[1,2]\\d*[1-9]0{4,5}"));
		
		assertEquals(false,"1010000".matches("[1,2]\\d*[1-9]0{2,3}"));
		assertEquals(true,"1010100".matches("[1,2]\\d*[1-9]0{2,3}"));
		assertEquals(true,"1011000".matches("[1,2]\\d*[1-9]0{2,3}"));
		assertEquals(false,"1000000".matches("[1,2]\\d*[1-9]0{2,3}"));
		
		assertEquals(false,"1010000".matches("[1,2]\\d*[1-9]0{0,1}"));
		assertEquals(false,"1010100".matches("[1,2]\\d*[1-9]0{0,1}"));
		assertEquals(false,"1011000".matches("[1,2]\\d*[1-9]0{0,1}"));
		assertEquals(false,"1000000".matches("[1,2]\\d*[1-9]0{0,1}"));
		assertEquals(true,"1010101".matches("[1,2]\\d*[1-9]0{0,1}"));
		assertEquals(true,"1010110".matches("[1,2]\\d*[1-9]0{0,1}"));
	}
}
