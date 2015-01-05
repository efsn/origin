package org.codeyn.util.yn;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.codeyn.util.Comparators;
import org.codeyn.util.MathUtil;
import org.codeyn.util.i18n.I18N;

public final class ArrYn {
  private ArrYn() {
  }

  public static final int[] clone(int[] a) {
    if (a == null || a.length == 0) {
      return a;
    }
    int[] r = new int[a.length];
    System.arraycopy(a, 0, r, 0, r.length);
    return r;
  }

  public static final int hash(final String s1, final String s2,
      final String[] a) {
    int len = a != null ? a.length : 0;
    int h = 0;
    h = hash(s1, h);
    h = 31 * h + '\t';
    h = hash(s2, h);
    h = 31 * h + '\t';
    for (int i = 0; i < len; i++) {
      h = hash(a[i], h);
      h = 31 * h + '\t';
    }
    return h;
  }

  public static final int hash(final String[] a) {
    if (a == null || a.length == 0) {
      return 0;
    }

    int len = a.length;
    int h = 0;
    for (int i = 0; i < len; i++) {
      h = hash(a[i], h);
      h = 31 * h + '\t';
    }

    return h;
  }

  /**
   * hash算法，一般在获得对象hashcode时使用
   * 使用老的hash值加一个字符串生成一个新的hash代码，使用该算法可以提高生产hash的效率
   * 如：对象class T{String f1;String f2}
   * 算法1：(f1+f2).hashCode(); 但该算法要生成StringBuffer对象，对于hashCode这种频繁调用的函数效率应尽量高
   * 算法2：ArrayFunc.hash(f1,f2.hashCode()); 不会生成新对象，效率提高
   * @param a
   * @param h
   * @return
   */
  public static final int hash(final String a, int h) {
    if (a == null || a.length() == 0) {
      return h;
    }
    int len = a.length();

    for (int i = 0; i < len; i++) {
      h = 31 * h + a.charAt(i);
    }
    return h;
  }
  
  /**
   * copy from Arrays jdk1.5!!
   * 
   * Returns a hash code based on the contents of the specified array.  If
   * the array contains other arrays as elements, the hash code is based on
   * their identities rather than their contents.  It is therefore
   * acceptable to invoke this method on an array that contains itself as an
   * element,  either directly or indirectly through one or more levels of
   * arrays.
   *
   * <p>For any two arrays <tt>a</tt> and <tt>b</tt> such that
   * <tt>Arrays.equals(a, b)</tt>, it is also the case that
   * <tt>Arrays.hashCode(a) == Arrays.hashCode(b)</tt>.
   *
   * <p>The value returned by this method is equal to the value that would
   * be returned by <tt>Arrays.asList(a).hashCode()</tt>, unless <tt>a</tt>
   * is <tt>null</tt>, in which case <tt>0</tt> is returned.
   *
   * @param a the array whose content-based hash code to compute
   * @return a content-based hash code for <tt>a</tt>
   * @see #deepHashCode(Object[])
   * @since 1.5
   */
  public static int hash(Object a[]) {
    if (a == null)
      return 0;

    int result = 1;

    for (int i = 0; i < a.length; i++) {
      Object element = a[i];
      result = 31 * result + (element == null ? 0 : element.hashCode());
    }

    return result;
  }
  
  public static final boolean compareArray(int[] ar1, int[] ar2) {
    if (ar1 == ar2 || (ar1 == null && ar2.length == 0)
        || (ar1.length == 0 && ar2 == null)) {
      return true;
    }
    if (ar1.length != ar2.length) {
      return false;
    }
    for (int i = 0, len = ar1.length; i < len; i++) {
      if (ar1[i] != ar2[i]) {
        return false;
      }
    }
    return true;
  }

  public static final boolean compareArray(String[] ar1, String[] ar2) {
    if (ar1 == ar2 || (ar1 == null && ar2.length == 0)
        || (ar1.length == 0 && ar2 == null)) {
      return true;
    }
    if (ar1.length != ar2.length) {
      return false;
    }
    for (int i = 0, len = ar1.length; i < len; i++) {
      if (!StrYn.compareStr(ar1[i], ar2[i]))
        return false;
    }
    return true;
  }
  
  /**
   * 与Arrays.equals不同的是，此函数认为null和空数组是相等的
   */
  public static final boolean compareArray(Object[] ar1, Object[] ar2) {
    if (ar1 == ar2 || (ar1 == null && ar2.length == 0)
        || (ar1.length == 0 && ar2 == null)) {
      return true;
    }
    return Arrays.equals(ar1, ar2);
  }

  public static final int[] str2int(String[] ss, int def) {
    if (ss == null) {
      return null;
    }
    int len = ss.length;
    int[] ii = new int[len];
    for (int i = 0; i < len; i++) {
      ii[i] = StrYn.str2int(ss[i], def);
    }
    return ii;
  }

  /**
   * 将一个对象数组加到一个list中
   * @param o
   * @param l
   */
  public static final void array2List(Object[] o, List l) {
    if (o == null || l == null || o.length == 0)
      return;
    for (int i = 0; i < o.length; i++) {
      l.add(o[i]);
    }
  }

  /**
   * 将字符串数组转换成整形数组，有可能有异常
   * @param ss
   * @return
   */
  public static final int[] str2int(String[] ss) {
    if (ss == null) {
      return null;
    }
    int len = ss.length;
    int[] ii = new int[len];
    for (int i = 0; i < len; i++) {
      ii[i] = Integer.parseInt(ss[i]);
    }
    return ii;
  }

  /**
   * 将数组ii中的每个元素都加上step
   * @param ii
   * @param step
   */
  public static final void offset(int[] ii, int step) {
    if (ii == null) {
      return;
    }
    int len = ii.length;
    for (int i = 0; i < len; i++) {
      ii[i] += step;
    }
  }

  /**
   * 将两个数组a1，a2合并生成一个新数组，并返回
   * 如{1}, {34,4} 将返回{1,34,4}
   * @param a1
   * @param a2
   * @return
   */
  public static final int[] merge(int[] a1, int[] a2) {
    int l1 = a1 == null ? 0 : a1.length;
    int l2 = a2 == null ? 0 : a2.length;
    if (l1 + l2 == 0)
      return null;
    int[] a = new int[l1 + l2];
    if (l1 != 0)
      System.arraycopy(a1, 0, a, 0, l1);
    if (l2 != 0)
      System.arraycopy(a2, 0, a, l1, l2);
    return a;
  }

  /**
   * 将两个数组合并生成一个新的数组，并返回
   * @param o1
   * @param o2
   * @return
   */
  public static final Object[] merge(Object[] o1, Object[] o2) {
    if (o1 == null && o2 == null) {
      return null;
    }
    int l1 = o1 == null ? 0 : o1.length;
    int l2 = o2 == null ? 0 : o2.length;
    if (l1 + l2 == 0)
      return null;
    Object[] r = (Object[]) Array.newInstance(o1 == null ? o2.getClass()
        .getComponentType() : o1.getClass().getComponentType(), l1 + l2);
    if (o1 != null) {
      System.arraycopy(o1, 0, r, 0, l1);
    }
    if (o2 != null) {
      System.arraycopy(o2, 0, r, l1, l2);
    }
    return r;
  }
  
  /**
   * 在数组ii中找寻i如果找到返回对应的序号，否则返回－1；
   * @param ii
   * @param i
   * @return
   */
  public static final int find(int[] ii, int i) {
    if (ii == null) {
      return -1;
    }
    int len = ii.length;
    for (int j = 0; j < len; j++) {
      if (ii[j] == i) {
        return j;
      }
    }
    return -1;
  }

  
  public static final int find(byte[] array,int beginIndex,int endIndex, int target) {
    if (array == null) {
      return -1;
    }
    for (int j = beginIndex; j <= endIndex; j++) {
      if (array[j] == target) {
        return j;
      }
    }
    return -1;
  }
  
  /**
   * 在数组ii中找寻i如果找到返回对应的序号，否则返回－1；
   * @param ii
   * @param i
   * @return
   */
  public static final int find(byte[] ii, int i) {
    if (ii == null) {
      return -1;
    }
    return find(ii,0,ii.length-1,i);
  }
  
  /**
   * 在数组ii中找寻i如果找到返回对应的序号，否则返回－1；
   * @param ii
   * @param i
   * @return
   */
  public static final int find(Object[] ii, Object i) {
    if (ii == null) {
      return -1;
    }
    return find(ii,0,ii.length-1,i);
  }
  

  /**在数组ii中找寻i如果找到返回对应的序号，否则返回－1；
   * @param array
   * @param beginIndex
   * @param endIndex
   * @param target
   * @return
   */
  public static int find(Object[] array, int beginIndex, int endIndex, Object target) {
    if (array == null) {
      return -1;
    }
    for (int j = beginIndex; j <= endIndex; j++) {
      //对象equals也可以认为是相等，例如某些对象实现了hashCode和equals函数，这些对象也可以认为是同一对象
      Object a = array[j];
      if (a == target || (a != null && a.equals(target)) ) {
        return j;
      }
    }
    return -1;

  }
  
  /**
   * 在数组中找字符串，如果找不到，返回-1.
   * @param ss
   * @param s
   * @return
   */
  public static final int find(String[] ss, String s) {
    return find(ss, s, false, -1);
  }

  /**
   * 在数组ss中查找s，如果找不到则返回def
   */
  public static final int find(String[] ss, String s, boolean ignoreCase,
      int def) {
    if (ss == null) {
      return def;
    }
    int len = ss.length;
    for (int j = 0; j < len; j++) {
      if (ss[j] == s) {
        return j;
      }
      if (s != null && ss[j] != null) {
        if (ignoreCase) {
          if (s.equalsIgnoreCase(ss[j])) {
            return j;
          }
        }
        else {
          if (s.equals(ss[j])) {
            return j;
          }
        }
      }
    }
    return def;
  }

  /**
   * 返回数组的不重复子集。
   */
  public static final String[] distinct(String[] ss){
    if ( ss==null || ss.length==0 ) return null;
    List dist = new java.util.ArrayList();
    for ( int i=0;i<ss.length;i++ ){
      if ( !dist.contains(ss[i]) )
        dist.add(ss[i]);
    }
    return (String[]) dist.toArray(new String[dist.size()]);
  }
  
  public static final void bubbleSort(List list, Comparator compare) {
    if (list == null || list.size() < 2)
      return;
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        if (compare.compare(list.get(i), list.get(j)) > 0)
          swap(list, i, j);
      }
    }
  }

  public static final void swap(List list, int i, int j) {
    Object obj = list.get(i);
    list.set(i, list.get(j));
    list.set(j, obj);
  }

  /**冒泡排序
   * @param list
   * @param compare
   */
  public static final void bubbleSort(Object[] list, Comparator compare) {
    if (list == null || list.length < 2)
      return;
    for (int i = 0; i < list.length; i++) {
      for (int j = i + 1; j < list.length; j++) {
        if (compare.compare(list[i], list[j]) > 0)
          swap(list, i, j);
      }
    }
  }
  
  /**采用多个排序器的冒泡排序
   * @param list
   * @param compare
   */
  public static final void bubbleSort(Object[] list, Comparator compare[]) {
    bubbleSort(list,new Comparators(compare));
  }

  public static final void swap(Object[] list, int i, int j) {
    Object obj = list[i];
    list[i] = list[j];
    list[j] = obj;
  }

  /**
   * 返回os中不为空的对象数组，如果os为空或长度为0，或元素都为空，返回null
   * 否则返回由不为空的元素组成的数组
   * @param os
   * @return
   */
  public static final Object[] excludeNull(Object[] os) {
    if (os == null || os.length == 0) {
      return null;
    }
    int len = os.length;
    int j = 0;
    for (int i = 0; i < len; i++) {
      if (os[i] != null) {
        if (i != j) {
          os[j] = os[i];
        }
        j++;
      }
    }
    if (j == 0) {
      return null;
    }
    if (j == len) {
      return os;
    }
    Object[] r = (Object[]) Array.newInstance(os.getClass().getComponentType(),
        j);
    System.arraycopy(os, 0, r, 0, j);
    return r;
  }

  /**
   * 返回strs中不为空并且长度不为0的字串数组，如果strs为空或长度为0，或元素都为空或长度为0，返回null
   * 否则返回由不为空的元素组成的数组
   * @param os
   * @return
   */
  public static final String[] excludeNullStrs(String[] strs) {
    return excludeNullStrs(strs,true);
  }
  
  /**
   * 返回字串数组,去除源数组中为null的字串,如果excludeEmpty=true,则也会去除长度为0的字串
   * 否则返回由不为空的元素组成的数组
   * @param os
   * @return
   */
  public static final String[] excludeNullStrs(String[] strs, boolean excludeEmpty) {
    if (strs == null || strs.length == 0) {
      return null;
    }
    int len = strs.length;
    int j = 0;
    for (int i = 0; i < len; i++) {
      String str = strs[i];
      boolean isnull = excludeEmpty ? StrYn.isNull(str) : str == null;
      if (!isnull) {
        if (i != j) {
          strs[j] = strs[i];
        }
        j++;
      }
    }
    if (j == 0) {
      return null;
    }
    if (j == len) {
      return strs;
    }
    String[] r = new String[j];
    System.arraycopy(strs, 0, r, 0, j);
    return r;
  }

    public static String array2Str(Object[] list, char sept) {
        return array2Str(list, StrYn.strofascii(sept));
    }

    /**
     * 将数组list用分隔符sept连接起来，null处理为“”
     * @param list
     * @param sept
     * @return
     */
    public static String array2Str(Object[] list, String sept) {
        if (list == null) {
            return null;
        }

        int length = list.length;
        if (length == 1) {
            return list[0] != null ? list[0].toString() : null;
        }

        StringBuffer sb = new StringBuffer(length * 10 + 32);
        for (int i = 0; i < length; i++) {
            Object element = list[i];
            if (element != null) {
                sb.append(element.toString()); //list[i]为空时会报错 2008.2.23
            }
            if (i < length - 1)
                sb.append(sept);
        }
        return sb.toString();

    }
  
  /**
   * 将浮点型数组list转换为一个指定分割符连接的字符串，不使用科学计数法，nan标识成空串
   */
  public static String doublearray2Str(double[] list, char sept) {
    if (list == null) {
      return null;
    }
    StringBuffer sb = new StringBuffer(list.length * (1 + 10));
    for (int i = 0; i < list.length; i++) {
      sb.append(StrYn.double2str(list[i],0,3,false));
      if (i < list.length - 1)
        sb.append(sept);
    }
    return sb.toString();
  }  
  
  public static String list2Str(List list, char sept) {
    Object[] obj = ArrYn.list2array(list);
    return array2Str(obj, sept);
  }
  
  public static String list2Str(List list, String sept) {
    Object[] obj = ArrYn.list2array(list);
    return array2Str(obj, sept);
  }
  
  public static String array2Str(Object items, char sept) {
    if (items == null) {
      return null;
    }
    int len = Array.getLength(items);
    StringBuffer sb = new StringBuffer(len * (1 + 10));
    for (int i = 0; i < len; i++) {
      Object o = Array.get(items,i);
      if (o!=null)
        sb.append(o.toString());
      if (i < len - 1)
        sb.append(sept);
    }
    return sb.toString();
  }

  /**将数组转成字符串，要求字符串编译之后能求出这个数组。
   * 比如对字符串数组，返回["1.23","3","4","",null,"5.76"]
   *    对数值数组，返回[1.23,3,4,5.76,NaN]
   * @param items 是一个数组
   * @return  
   */
  public static final String array2ExpStr(Object items, String sept) {
    if (items == null) {
      return null;
    }
    int len = Array.getLength(items);
    if(len==0) return "[]"; //返回长度为0的数组
    
    Object o = Array.get(items, 0);
    boolean isNum = o instanceof Number;

    StringBuffer sb = new StringBuffer(len * (1 + 10));
    sb.append("[");
    if(isNum){
        for (int i = 0; i < len; i++) {
        Object v = Array.get(items,i);
        
        if(v!=null){
          sb.append(StrYn.object2str(v));
        }else{
            sb.append(Double.NaN);
        }
        
        if (i < len - 1) sb.append(sept);
      }
    }else{
        for (int i = 0; i < len; i++) {
        Object v = Array.get(items,i);
        
        if(v!=null){
            sb.append("\"");
          sb.append(StrYn.object2str(v));
          sb.append("\"");
        }else{
            sb.append("null");
        }
        
        if (i < len - 1) sb.append(sept);
      }
    }
    sb.append("]");
    return sb.toString();
  }

  public static String arrayExcludeNull2Str(Object[] list, char sept) {
    return array2Str(excludeNull(list), sept);
  }

  public static final String array2displaystr(Object[] datas) {
    if (datas == null || datas.length == 0) {
      return "[]";
    }
    StringBuffer r = new StringBuffer();
    r.append("[");
    for (int i = 0; i < datas.length && i < 11; i++) {
      r.append(datas[i]).append(',');
    }
    r.setCharAt(r.length() - 1, ']');
    return r.toString();
  }

  /**
   * 返回os从off开始len长度的元素
   * @param os
   * @param off
   * @param len
   * @return
   */
  public static final Object[] subElement(Object[] os, int off, int len) {
    Object obj = os;
    return (Object[]) subElement(obj,off,len);
  }
  
  public static final Object subElement(Object source, int off, int len) {
    if (source == null)
      return null;
    if (!source.getClass().isArray()) {
      //throw new RuntimeException("源对象不是数组");
      throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.1", "源对象不是数组 "));
    }
    int slen = Array.getLength(source);
    if (slen == 0 || len <= 0 || off >= slen) {
      return null;
    }
    off = off < 0 ? 0 : off;
    len = slen - off < len ? slen - off : len;
    Object ods = Array.newInstance(source.getClass().getComponentType(), len);
    System.arraycopy(source, off, ods, 0, len);
    return ods;
  }
  
  /**
   * 将字串数组用指定的符号连接成一个字串返回。
   */
  public static final String join(String[] arr, String delimeter){
    if ( arr==null ) return null;
    StringBuffer buf = new StringBuffer();
    for ( int i=0;i<arr.length;i++ ){
      if ( i>0 )
        buf.append(delimeter);
      if ( arr[i]==null || arr[i].length()==0 )
        buf.append("");
      else
        buf.append(arr[i]);
    }
    return buf.toString();
  }
  
  /**
   * 反转数组
   */
  public static final void reverse(double[] dd){
    if (dd==null || dd.length<=1)
      return ;
    int sz = dd.length;
    for (int i = 0, len = sz/2; i < len; i++) {
      double t = dd[i];
      dd[i] = dd[sz-i-1];
      dd[sz-i-1] = t;
    }
  }
  
  public static final void reverse(long[] dd){
    if (dd==null || dd.length<=1)
      return ;
    int sz = dd.length;
    for (int i = 0, len = sz/2; i < len; i++) {
      long t = dd[i];
      dd[i] = dd[sz-i-1];
      dd[sz-i-1] = t;
    }
  }
  
  public static final void reverse(int[] dd){
    if (dd==null || dd.length<=1)
      return ;
    int sz = dd.length;
    for (int i = 0, len = sz/2; i < len; i++) {
      int t = dd[i];
      dd[i] = dd[sz-i-1];
      dd[sz-i-1] = t;
    }
  }
  
  public static final void reverse(Object[] os){
    if (os==null || os.length<=1)
      return ;
    int sz = os.length;
    for (int i = 0, len = sz/2; i < len; i++) {
      Object t = os[i];
      os[i] = os[sz-i-1];
      os[sz-i-1] = t;
    }
  }
  
    public static final void reverse(List a) {
        int sz = a.size();
        for (int i = 0, len = sz / 2; i < len; i++) {
            Collections.swap(a, i, sz-i-1);
        }
    }

    /**
     * 排序一个对象，a可能是一个原始数组对象，也可能是List或者是Object[]
     * @param a
     */
    public static final void reverse(Object a) {
        if (a == null)
            return;

        Class cls = a.getClass();

        if (cls.isArray()) {
            cls = cls.getComponentType();
            if (cls == double.class) {
                ArrYn.reverse((double[]) a);
            }
            else if (cls == int.class) {
                ArrYn.reverse((int[]) a);
            }
            else if (cls == long.class) {
                ArrYn.reverse((long[]) a);
            }
            else if (Object.class.isAssignableFrom(cls)) {
                ArrYn.reverse((Object[]) a);
            }
            else {
                int sz = Array.getLength(a);

                for (int i = 0, len = sz / 2; i < len; i++) {
                    Object t = Array.get(a, i);
                    Array.set(a, i, Array.get(a, sz - i - 1));
                    Array.set(a, sz - i - 1, t);
                }
            }
        }else if (a instanceof List){
            reverse((List)a);
        }else{
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * 判断数组a中的元素个数，如果distinct为true，那么多个相同的元素只计数一次
     * @param a
     * @return
     */
    public static int count(int[] a, boolean distinct) {
        if (a == null || a.length == 0) {
            return 0;
        }
        if (!distinct) {
            return a.length;
        }
        int length = a.length;
        int r = 0;
        for (int i = 0; i < length; i++) {
            if (indexOf(a, a[i], 0, i - 1) < 0) {
                r++;
            }
        }
        return r;
    }

    public static int indexOf(int[] a, int i, int fromi, int toi) {
        for (int j = fromi; j <= toi; j++) {
            if (a[j] == i)
                return j;
        }
        return -1;
    }
    
    /**
     * 判断数组a中的元素个数，如果distinct为true，那么多个相同的元素只计数一次
     * @param a
     * @return
     */
    public static int count(long[] a, boolean distinct) {
        if (a == null || a.length == 0) {
            return 0;
        }
        if (!distinct) {
            return a.length;
        }
        int length = a.length;
        int r = 0;
        for (int i = 0; i < length; i++) {
            if (indexOf(a, a[i], 0, i - 1) < 0) {
                r++;
            }
        }
        return r;
    }

    public static int indexOf(long[] a, long i, int fromi, int toi) {
        for (int j = fromi; j <= toi; j++) {
            if (a[j] == i)
                return j;
        }
        return -1;
    }
    
    /**
     * 判断数组a中的元素个数，忽略NaN，如果distinct为true，那么多个相同的元素只计数一次
     * @param a
     * @return
     */
    public static int count(double[] a, boolean distinct) {
        if (a == null || a.length == 0) {
            return 0;
        }
        int length = a.length;
        int r = 0;
        for (int i = 0; i < length; i++) {
            double d = a[i];
            if (!Double.isNaN(d)) {
                if (!distinct || indexOf(a, d, 0, i - 1) < 0) {
                    r++;
                }
            }
        }
        return r;
    }

    public static int indexOf(double[] a, double i, int fromi, int toi) {
        for (int j = fromi; j <= toi; j++) {
            if (StrYn.compareDouble(a[j], i) == 0)
                return j;
        }
        return -1;
    }
    
    public static int count(Object[] a, boolean distinct) {
        if (a == null || a.length == 0) {
            return 0;
        }
        int length = a.length;
        int r = 0;
        for (int i = 0; i < length; i++) {
            Object d = a[i];
            if (d!=null&&(!(d instanceof Double && ((Double)d).isNaN()))) {
                if (!distinct || indexOf(a, d, 0, i - 1) < 0) {
                    r++;
                }
            }
        }
        return r;
    }

    public static int indexOf(Object[] a, Object i, int fromi, int toi) {
        for (int j = fromi; j <= toi; j++) {
            if (StrYn.compareObject(a[j], i) == 0)
                return j;
        }
        return -1;
    }
    
    public static int count(List a, boolean distinct) {
        if (a == null || a.size() == 0) {
            return 0;
        }
        int length = a.size();
        int r = 0;
        for (int i = 0; i < length; i++) {
            Object d = a.get(i);
            if (d!=null&&(!(d instanceof Double && ((Double)d).isNaN()))) {
                if (!distinct || indexOf(a, d, 0, i - 1) < 0) {
                    r++;
                }
            }
        }
        return r;
    }

    public static int indexOf(List a, Object i, int fromi, int toi) {
        for (int j = fromi; j <= toi; j++) {
            if (StrYn.compareObject(a.get(j), i) == 0)
                return j;
        }
        return -1;
    }
    
    /**
     * 判断数组a中的元素个数，忽略NaN和null，如果distinct为true，那么多个相同的元素只计数一次
     * a可能是一个原始数组对象，也可能是List或者是Object[]
     * @param a
     */
    public static final int count(Object a, boolean distinct) {
        if (a == null)
            return 0;

        Class cls = a.getClass();

        if (cls.isArray()) {
            cls = cls.getComponentType();
            if (cls == double.class) {
                return ArrYn.count((double[]) a, distinct);
            }
            else if (cls == int.class) {
                return ArrYn.count((int[]) a, distinct);
            }
            else if (cls == long.class) {
                return ArrYn.count((long[]) a, distinct);
            }
            else if (Object.class.isAssignableFrom(cls)) {
                return ArrYn.count((Object[]) a, distinct);
            }
            else {
                int length = Array.getLength(a);
                int r = 0;
                for (int i = 0; i < length; i++) {
                    Object d = Array.get(a, i);
                    if (d != null) {
                        if (!distinct || indexOf(a, d, 0, i - 1) < 0) {
                            r++;
                        }
                    }
                }
                return r;
            }
        }else if (a instanceof List){
            return count((List)a, distinct);
        }else{
            throw new UnsupportedOperationException();
        }
    }

    private static int indexOf(Object a, Object o, int fromi, int toi) {
        for (int j = fromi; j < toi; j++) {
            if (StrYn.compareObject(Array.get(a, j), o) == 0)
                return j;
        }
        return -1;
    }

/**
   * 将一个数组对象转换为double数组
   */
  public static double[] array2doubleArray(Object items) {
    if (items == null) {
      return null;
    }
    if (items.getClass().getComponentType()==double.class)
      return (double[]) items;
    int len = Array.getLength(items);
    double[] r = new double[len];
    for (int i = 0; i < len; i++) {
        /**
         * 如果items是一个Double数组，那么Array.getDouble会出异常
         */
      r[i] = ((Number)Array.get(items, i)).doubleValue();
    }
    return r;
  }
  
  /**
   * 将一个数组对象转换为int数组
   */
  public static int[] array2intArray(Object items) {
    if (items == null) {
      return null;
    }
    if (items.getClass().getComponentType()==int.class)
      return (int[]) items;
    int len = Array.getLength(items);
    int[] r = new int[len];
    for (int i = 0; i < len; i++) {
        /**
         * 如果items是一个Double数组，那么Array.getDouble会出异常
         */
      r[i] = (int) ((Number)Array.get(items, i)).doubleValue();
    }
    return r;
  }

  /**
   * 将一个数组对象转换为List对象
   */
  public static List array2list(Object items, List list) {
    if (items == null) {
      return list;
    }
    int len = Array.getLength(items);
    if (list==null){
      list = new ArrayList(len);
    }
    for (int i = 0; i < len; i++) {
      list.add(Array.get(items,i));
    }
    return list;
  }
  
  /**
   * 将一个数组类型的对象转换为Object[]数组，如果内部有基本类型，将被包装为对象
   * @param items
   * @return
   */
  public static Object[] array2ObjArray(Object items) {
    if (items == null) {
      return null;
    }
    Class ct = items.getClass().getComponentType();
    if (ct == int.class || ct == long.class || ct == double.class || ct == byte.class || ct == char.class
        || ct == short.class || ct == float.class || ct == boolean.class) {
      int len = Array.getLength(items);
      Object[] objs = new Object[len];
      for (int i = 0; i < len; i++) {
        objs[i] = Array.get(items, i);
      }
      return objs;
    }
    return (Object[]) items;
  }
  
    /**
    将一个数组对象转换成字符串数组
  */
  public static String[] array2stringArray(Object items) {
    if (items == null) {
      return null;
    }
    if (items.getClass().getComponentType()==String.class)
      return (String[]) items;
    int len = Array.getLength(items);
    String[] r = new String[len];
    for (int i = 0; i < len; i++) {
      Object o = Array.get(items,i);
      r[i] = o!=null?o.toString():null;
    }
    return r;
  }

  /**
   * 让一个数组有至少minCapacity个元素
   */
  public static Object ensureCapacity(Object ar, int minCapacity) {
    int oldCapacity = Array.getLength(ar);
    if (minCapacity > oldCapacity) {
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity)
        newCapacity = minCapacity;
      ar = setCapacity(ar, newCapacity);
    }
    return ar;
  }
  
  /**
   * 让一个数组的长度为capacity个元素
   */
  public static Object setCapacity(Object ar, int capacity) {
    int len = Array.getLength(ar);
    if (len==capacity)
      return ar;
    
    Object nar = java.lang.reflect.Array.newInstance(ar.getClass().getComponentType(), capacity);
    System.arraycopy(ar, 0, nar, 0, Math.min(capacity,len));
    
    return nar;
  }

  /**
   * 求数组ar中最大的元素并返回，ar如果为null或不是数组，都触发异常
   */
  public final static Object max(Object ar){
    Class cls = _checkIsArray(ar);
    int len = Array.getLength(ar);
    if (cls == double.class){//优化速度
      return new Double(MathUtil.max((double[]) ar,len));
    }
    if (len == 0) {
      return null;
    }
    
    Object r = Array.get(ar,0);
    for (int i = 1; i < len; i++) {
      Object v = Array.get(ar,i);
      if (StrYn.compareObject(r,v)<0) {
        r = v;
      }
    }
    return r;
  }
  
  /**
   * 求数组ar中最小的元素并返回，ar如果为null或不是数组，都触发异常
   */
  public final static Object min(Object ar){
    Class cls = _checkIsArray(ar);
    int len = Array.getLength(ar);
    if (cls == double.class){
      return new Double(MathUtil.min((double[]) ar,len));
    }
    if (len == 0) {
      return null;
    }
    
    Object r = Array.get(ar,0);
    for (int i = 1; i < len; i++) {
      Object v = Array.get(ar,i);
      if (StrYn.compareObject(r,v)>0) {
        r = v;
      }
    }
    return r;
  }
  
  /**
   * 返回数组ar的维数，一维数组返回1，二维数组返回2....，不是数组返回0；
   */
  public static int getArrayDimension(Object ar) {
    if (ar==null)
      return 0;
    Class cls = ar.getClass();
    if (!cls.isArray()){
      return 0;
    }
    if (Array.getLength(ar)==0){
      return 1;
    }
    return 1+getArrayDimension(Array.get(ar,0));
  }
  
  private static Class _checkIsArray(Object ar) {
    if (ar==null)
      throw new NullPointerException();
    Class cls = ar.getClass();
    if (!cls.isArray())
      throw new IllegalArgumentException(ar + " is not a array!");
    return cls;
  }
  
  /**
   * 将list装换为指定Class的数组，并返回
   * 如果list为null或没有元素,则返回null
   * 如果有元素,并且所有的元素都为null，而且没有指定Class，抛出异常
   * 所有的元素必须都是同一种类型
   * @param list
   * @return
   */
  public static Object[] list2array(Collection list) {
    return list2array(list,null);
  }
  public static Object[] list2array(Collection list, Class c) {
    if (list == null || list.size() == 0)
      return null;
    if (c == null) {
      Object obj = null;
      for (Iterator iter = list.iterator(); iter.hasNext();) {
        obj = iter.next();
        if (obj != null) {
          break;
        }
            }
      
      if (obj == null) {
        //throw new RuntimeException("元素都为null");
        throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.2", "元素都为null"));
      }
      c = obj.getClass();
    }
    Object[] obs = (Object[]) Array.newInstance(c, list.size());
    list.toArray(obs);
    return obs;
  }
  
  /**
   * 将集合list的大小改为size，如果size比list.size()小，则不改变原来的list
   * 如果size比list.size()大，则增加list的长度，填充null
 * @param lst 进行操作的集合
 * @param size 集合的大小
 */
public static void fillNull(List lst, int size){
    for(int i = lst.size(); i < size; i++){
      lst.add(null);
    }
  }
  
  /**
   * 创建指定长度的链表，链表的每个元素用null填充。
   * @param size 链表长度。注意：这不是容量。
   * @return 返回的链表的size为指定长度。
   */
  public static List createNullElementList(int size){
    List lst = new ArrayList(size);
    for(int i = lst.size(); i < size; i++){
      lst.add(null);
    }
    return lst;
  }
  
  public static Integer [] int2Objs(int [] ii){
    Integer [] objs = new Integer[ii.length];
    for (int i = 0; i < objs.length; i++) {
      objs[i] = StrYn.intobj(ii[i]);  
    }
    return objs;
  }
  
  public static int [] obj2int(Integer [] ii){
    int [] objs = new int[ii.length];
    for (int i = 0; i < objs.length; i++) {
      objs[i] = ii[i].intValue();
    }
    return objs;
  }
  
  public static void main(String args[]){
    String[] a= new String[]{null};
    System.out.println(array2ExpStr(a, ","));
  }
  
//  public static int indexOf(byte[] source, int sourceOffset, int sourceCount, byte[] target, int targetOffset,
//      int targetCount, int fromIndex) {
//    if (fromIndex >= sourceCount) {
//      return (targetCount == 0 ? sourceCount : -1);
//    }
//    if (fromIndex < 0) {
//      fromIndex = 0;
//    }
//    if (targetCount == 0) {
//      return fromIndex;
//    }
//
//    byte first = target[targetOffset];
//    int max = sourceOffset + (sourceCount - targetCount);
//
//    for (int i = sourceOffset + fromIndex; i <= max; i++) {
//      /* Look for first character. */
//      if (source[i] != first) {
//        while (++i <= max && source[i] != first)
//          ;
//      }
//
//      /* Found first character, now look at the rest of v2 */
//      if (i <= max) {
//        int j = i + 1;
//        int end = j + targetCount - 1;
//        for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
//          ;
//
//        if (j == end) {
//          /* Found whole string. */
//          return i - sourceOffset;
//        }
//      }
//    }
//    return -1;
//  }
  
  /**
   * 返回target在source中从fromIndex开始的位置,如果没有返回-1
   * @param source  数组
   * @param target  数组
   * @param fromIndex  开始位置
   * @return
   * @deprecated
   */
  public static int indexOf(Object source, Object target) {
    return indexOf(source, target, 0);
  }

  /**
   * @deprecated
   * @param source
   * @param target
   * @param fromIndex
   * @return
   */
  public static int indexOf(Object source, Object target, int fromIndex) {
    return indexOf(source, 0, Integer.MAX_VALUE, target, 0, Integer.MAX_VALUE, fromIndex);
  }
  /**
   * 返回target[targetOffset..targetCount]在数组source[sourceOffset..sourceCount]中从fromIndex开始的位置,如果没有返回-1
   * @param source
   * @param sourceOffset
   * @param sourceCount
   * @param target
   * @param targetOffset
   * @param targetCount
   * @param fromIndex
   * @return
   * @deprecated  如果数组source是基础类型,会在搜索过程中创建许多对象,降低效率,不推荐使用
   */
  public static int indexOf(Object source, int sourceOffset, int sourceCount, Object target, int targetOffset,
      int targetCount, int fromIndex) {
    //检查source和target对象
    if (source == null || target == null)
      return -1;
    if (!source.getClass().isArray()) {
      //throw new RuntimeException("源对象不是数组");
      throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.3", "源对象不是数组"));
    }
    if (!target.getClass().isArray()) {
      //throw new RuntimeException("目标对象不是数组");
      throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.4", "目标对象不是数组"));
    }
    //检查sourceCount和targetCount是否小于0
    if (sourceCount < 0) {
      //throw new RuntimeException("源数组长度小于0");
      throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.5", "源数组长度小于0"));
    }
    if (targetCount < 0) {
      //throw new RuntimeException("目标数组长度小于0");
      throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.6", "目标数组长度小于0 "));
    }
    //重设数组长度
    int slen = Array.getLength(source);
    int tlen = Array.getLength(target);
    if (slen < sourceCount) {
      sourceCount = slen;
    }
    if (tlen < targetCount) {
      targetCount = tlen;
    }
    //如果源数组设置搜索长度为0,直接返回-1
    if (sourceCount == 0){
      return -1;
    }
    //检查sourceOffset和targetOffset
    if (sourceOffset < 0) {
      sourceOffset = 0;
    }
    if (targetOffset < 0) {
      targetOffset = 0;
    }
    if (sourceOffset >= sourceCount) {
      //throw new RuntimeException("源数组开始查找位置大于源数组长度");
      throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.7", "源数组开始查找位置大于源数组长度 "));
    }
    if (targetOffset >= targetCount) {
      //throw new RuntimeException("目标开始查找位置大于目标数组长度");
      throw new RuntimeException(I18N.getString("com.esen.util.ArrayFunc.8", "目标开始查找位置大于目标数组长度 "));
      
    }
    //开始查找
    if (fromIndex >= sourceCount) {
      return (targetCount == 0 ? sourceCount : -1);
    }
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    if (targetCount == 0) {
      return fromIndex;
    }
    //开始比较数组
    Object first = Array.get(target, targetOffset);
    int max = sourceOffset + (sourceCount - targetCount);

    for (int i = sourceOffset + fromIndex; i <= max; i++) {
      /* Look for first character. */
      if (!objectEquals(Array.get(source, i), first)) {
        while (++i <= max && !objectEquals(Array.get(source, i), first))
          ;
      }

      /* Found first character, now look at the rest of v2 */
      if (i <= max) {
        int j = i + 1;
        int end = j + targetCount - 1;
        for (int k = targetOffset + 1; j < end && objectEquals(Array.get(source, j), Array.get(target, k)); j++, k++)
          ;

        if (j == end) {
          /* Found whole string. */
          return i - sourceOffset;
        }
      }
    }
    return -1;
  }
  
  /**
   * 比较两个对象是否相等
   * @param o1
   * @param o2
   * @return
   */
  private static boolean objectEquals(Object o1, Object o2) {
    if (o1 == o2)
      return true;
    if (o1 == null) {
      return o2 == null ? true : o2.equals(o1);
    }
    return o1.equals(o2);
  }
}