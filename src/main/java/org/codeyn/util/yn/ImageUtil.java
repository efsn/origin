package org.codeyn.util.yn;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.codeyn.util.YnByteArrayOutStream;

public class ImageYn {
  /**
   * 将图片转为jpg
   * @param data 图片的二进制数据
   * @return
   * @throws IOException
   */
  public static byte[] toJpg(byte data[]) throws IOException{
    ByteArrayInputStream is = new ByteArrayInputStream(data);
    MemoryCacheImageInputStream mis = new MemoryCacheImageInputStream(is);
    BufferedImage bi = ImageIO.read(mis);
    is.close();
    
    YnByteArrayOutStream os = new YnByteArrayOutStream(data.length);
    ImageIO.write(bi, "jpg", os);
    os.close();
    return os.toByteArray();
  }
  
  public static byte[] readFile(String fileName) throws IOException{
    FileInputStream is = new FileInputStream(fileName);
    byte[] b = null;
    try{
        b = new byte[is.available()];
      is.read(b);
    }finally{
      is.close();
    }
    return b;
  }
  
}