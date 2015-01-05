package com.esen.io;

public interface ResourceSaver {  
  /**
   * 添加一个缓冲对象，此方法返回的是一个src,此src可以直接显示在客户端
   * id是一个字符串，唯一表示obj，当id已存在时，此函数只是增加一个引用计数，下载一次引用计数减1，引用计数为0时此对象会被删除,前提是deleteWhenDownloaded为真
   *   也可以为null此时此函数自动生成一个不唯一的id返回
   * obj可以是
   *   File，表示临时文件
   *   ImageInReport 表示图片文件
   *   byte[] 表示二进制信息
   *   HttpDownloadCachable 表示一个自定义的实现
   * contentType 表示此内容的类型，也用于返回给客户端时使用
   * 
   */
  public String saveObj(String id, Object obj, String contentType);
}
