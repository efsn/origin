package com.esen.jdbc.timesten;

import java.lang.reflect.Method;
import java.sql.*;

public class TestTimesTen  {
  public void testConnection() throws Exception{
    Class.forName("com.timesten.jdbc.TimesTenDriver");
    String url = "jdbc:timesten:direct:dsn=ttdemo";
    Connection con = DriverManager.getConnection(url,"olap20","97142024");
    try{
      DatabaseMetaData dbmd = con.getMetaData();
      Method[] mths = dbmd.getClass().getMethods();
      for(int i=0;i<mths.length;i++){
        String name = mths[i].getName();
        Object v = null;
        if(mths[i].getParameterTypes().length==0){
          try{
           v = mths[i].invoke(dbmd, null);
          }catch(Exception ex){
            
          }
        }
        System.out.println(name+":\t"+v);
      }
      Statement stat = con.createStatement();
      ResultSet rs = stat.executeQuery("select * from dim_hy");
      while(rs.next()){
        System.out.println(rs.getString(1)+"\t"+rs.getString(2));
      }
      stat.close();
    }finally{
      con.close();
    }
  }
  public static void main(String[] args) throws Exception{
    TestTimesTen tt = new TestTimesTen();
    tt.testConnection();
  }
}
