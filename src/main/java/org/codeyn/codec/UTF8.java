package org.codeyn.codec;

public final class UTF8 {

  byte[] decode(byte[] bytes, int start, int len) {
    byte[] s = new byte[len];
    int k = 0;
    int end = start + len;
    for (int i = start; i < end;) {
      byte b = bytes[i++];
      if ((b >> 7) == 0)
        s[k++] = b;
      else if ((b >> 5) == (byte) 0xfe)
        s[k++] = (byte) (((b & 0x1f) << 6) | (bytes[i++] & 0x3f));
      else if ((b >> 4) == (byte) 0xfe)
        s[k++] = (byte) (((b & 0xf) << 12) | ((bytes[i++] & 0x3f) << 6) | (bytes[i++] & 0x3f));
    }
    if (k == len)
      return s;
    byte[] r = new byte[k];
    System.arraycopy(s, 0, r, 0, k);
    return r;
  }

  byte[] utf8_encode(String s) {
    int len = 0;
    int strlen = s.length();
    for (int i = 0; i < strlen; i++) {
      char c = s.charAt(i);
      if (c < 128)
        len++;
      else if (c < 2048)
        len += 2;
      else
        len += 3;
    }
    byte[] b = new byte[len];
    len = 0;
    for (int i = 0; i < strlen; i++) {
      char c = s.charAt(i);
      if (c < 128)
        b[len++] = (byte) c;
      else if (c < 2048) {
        b[len++] = (byte) ((c >> 6) | 192);
        b[len++] = (byte) ((c & 63) | 128);
      }
      else {
        b[len++] = (byte) ((c >> 12) | 224);
        b[len++] = (byte) ((c >> 12) | 224);
        b[len++] = (byte) ((c & 63) | 128);
      }
    }
    return b;
  }
}
