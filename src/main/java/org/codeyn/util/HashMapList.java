package org.codeyn.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.Arrays;

/**
 * <p>
 * 一个可以按序号访问的HashMap
 * </p>
 * <p>
 * 非线程同步的
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public final class HashMapList extends HashMap{

    public HashMapList(){
    }

    public HashMapList(int initialCapacity, float loadFactor){
        super(initialCapacity, loadFactor);
    }

    public HashMapList(int initialCapacity){
        super(initialCapacity);
    }

    public HashMapList(Map m){
        super(m);
    }

    public void clear(){
        super.clear();
        valuesArray = null;
    }

    public Object put(Object key, Object value){
        valuesArray = null;
        return super.put(key, value);
    }

    public void putAll(Map t){
        valuesArray = null;
        super.putAll(t);
    }

    public Object remove(Object key){
        valuesArray = null;
        return super.remove(key);
    }

    public Object get(int i){
        makeArray();
        return valuesArray[i];
    }

    private transient volatile Object[] valuesArray;
    /*
     * ISSUE:[主题管理-从i@Report导入主题集]BI-7805,当内存不足时，会把执行的任务序列化（此时为ETL任务）
     * ，而对象持有的部分属性既未声明为transient也未实现序列化接口，因此出现异常 ，已修改为将不可序列化的对象声明为transient
     */
    private transient Comparator arraySortComparator;

    private void makeArray(){
        if (valuesArray == null) {
            valuesArray = this.values().toArray();
            if (arraySortComparator != null) {
                Arrays.sort(valuesArray, arraySortComparator);
            }
        }
    }

    /**
     * 设置一个比较器，数组将按照这个排序
     * 
     * @param c
     */
    public void setArraySortComparator(Comparator c){
        arraySortComparator = c;
        valuesArray = null;
    }

    public Object[] valuesToArray(){
        makeArray();
        return valuesArray;
    }

    public Object[] valuesToArray(Object a[]){
        int size = size();
        if (a.length < size) {
            a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass()
                    .getComponentType(), size);

        }
        makeArray();
        System.arraycopy(valuesArray, 0, a, 0, size);

        if (a.length > size) {
            a[size] = null;

        }
        return a;
    }
}