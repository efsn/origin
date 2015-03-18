package com.esen.jdbc.data;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DataCopyMssql2005Test extends DataCopyTest {

  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.2:1433/shlbak;TDS=8.0;SendStringParametersAsUnicode=true",
        "sa", "sa","debug");
  }

}
