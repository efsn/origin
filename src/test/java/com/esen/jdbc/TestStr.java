package com.esen.jdbc;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class TestStr {

  /**
   * @param args
   * @throws UnsupportedEncodingException 
   */
  public static void main(String[] args) throws UnsupportedEncodingException {
    /*Double d = new Double("12");
    System.out.println(d.compareTo(new Double(Double.NaN)));
    String value = "2001*";
    System.out.println(value.replaceAll("[\\*\\?%_-]", ""));
    value = "2001??";
    System.out.println(value.replaceAll("[\\*\\?%_-]", ""));
    value = "2001%";
    System.out.println(value.replaceAll("[\\*\\?%_-]", ""));
    value = "2001__";
    System.out.println(value.replaceAll("[\\*\\?%_-]", ""));
    value = "2001-01";
    System.out.println(value.replaceAll("[\\*\\?%_-]", ""));
    System.out.println(Pattern.matches("[0-9]{4}[0-9]{2}-*", "200401--"));
    System.out.println(Pattern.matches("[0-9]{4}[0-9]{2}-*", "200401"));
    
    String ss = "case when count(*)=0 then NULL else case when count(*)=0 then null else (sum( ABS(a.D5-INNERZB4574)))/count(*) end  end";
    String match = ".*[^A-Za-z0-9_]"+"INNERZB4574"+"[^A-Za-z0-9_].*";
    System.out.println(ss.matches(match));
    
    System.out.println(Pattern.matches("((?i)insert)a", "inSertA"));
    
    String str = new String("虓亭区".getBytes("GBK"),"iso8859_1");
    System.out.println(new String(str.getBytes("iso8859_1")));
    */
    TestStr ts = new TestStr();
    for(int i=0;i<255;i++){
      StringBuffer sb = new StringBuffer();
      sb.append("insert into dim_ecode (cod,name)values(");
      sb.append("'").append(i+1);
      sb.append("','").append(ts.getAutoTag(i)).append("');");
      System.out.println(sb);
    }
    /*for(int i=0;i<255;i++){
      System.out.print(i+1);
      System.out.print("\t");
      System.out.print(ts.getAutoTag(i));
      System.out.println();
    }*/
    
  }
  private String getAutoTag(int n) {
    int k = n/26;
    int m = n%26;
    if(k>0){
      return getAutoTag(k-1)+String.valueOf((char) ('A' + m));
    }else{
      return String.valueOf((char) ('A' + m));
    }
  }
}
