package org.codeyn.util;

/**
 * 格式化输出，支持%d %d %s的简单形式
 */
public class Formatter {
  StringBuffer sb;

  public String toString() {
    return sb.toString();
  }

  protected static final String TAG_INT = "%d";

  protected static final String TAG_FLOAT = "%f";

  protected static final String TAG_DTR = "%s";

  public StringBuffer setNext(int v) {
    int index = sb.indexOf(TAG_INT);
    if (index >= 0)
      sb.replace(index, index + 2, Integer.toString(v));
    return sb;
  }

  public StringBuffer setNext(double v) {
    int index = sb.indexOf(TAG_FLOAT);
    if (index >= 0)
      sb.replace(index, index + 2, Double.toString(v));
    return sb;
  }

  static final String TAG_NULL = "";

  public StringBuffer setNext(String v) {
    int index = sb.indexOf(TAG_DTR);
    if (index >= 0)
      sb.replace(index, index + 2, v == null ? TAG_NULL : v);
    return sb;
  }

  public Formatter(String sb) {
    setValue(sb);
  }

  public void setValue(String s) {
    this.sb = new StringBuffer(s);
  }

  public StringBuffer append(boolean arg0) {
    return sb.append(arg0);
  }

  public StringBuffer append(char arg0) {
    return sb.append(arg0);
  }

  public StringBuffer append(int arg0) {
    return sb.append(arg0);
  }

  public StringBuffer append(String arg0) {
    return sb.append(arg0);
  }

  public StringBuffer append(StringBuffer arg0) {
    return sb.append(arg0);
  }
}