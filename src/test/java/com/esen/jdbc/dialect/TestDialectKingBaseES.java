package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectKingBaseES extends TestDialect {
  public ConnectionFactory createConnectionFactory() {
    return new SimpleConnectionFactory(
        "com.kingbase.Driver",
        "jdbc:kingbase://192.168.1.247/testdb",
        "test", "test","debug");
  }
}
