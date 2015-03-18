package com.esen.jdbc.data;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DataCopySybaseTest extends DataCopyTest {

  public ConnectionFactory createConnectionFactory() {
    /**
     * sybase使用utf8会有中文问题，有些汉字显示不出来，比如地震的”震“
     * 这里改为utf8就会有错误；
     */
    return new SimpleConnectionFactory(
        "com.sybase.jdbc2.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.42:5000/bidb?charset=cp936",
        "test", "111111","debug");
  }

}
