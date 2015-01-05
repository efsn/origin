package org.codeyn.util.io;

import java.io.InputStream;

/**
 * olapresult接口中可能需要返回大字段的信息，大字段的内容以此接口的形式返回 
 *
 */
public interface BlobValue {
  /**
   * 获取一个输入的读入接口，此函数可以调用多次。
   * 返回的流没有经过buffered流包装，如果调用者不是成批的读取其中的内容，那么调用者应该自己用buffered流包装
   * @return
   */
  public InputStream getInputStream();

  /**
   * 获取数据的长度
   * @return
   */
  public long getLength();
}
