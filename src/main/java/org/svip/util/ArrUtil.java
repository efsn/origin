package org.svip.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author Blues
 * @version 1.0
 * Created on 2014/9/14
 */
public final class ArrUtil<T> {

    private T[] arr;

    private ArrUtil(T[] arr) {
        this.arr = arr;
    }

    public static <T> ArrUtil<T> instance(T[] arr) {
        return new ArrUtil<T>(arr);
    }

    public static String concat(String[] arr, String wrap, String append) {
        StringBuffer sb = new StringBuffer();
        for (String s : arr) {
            sb.append(wrap).append(StrUtil.getDbName(s)).append(wrap).append(append);
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Move out value
     * Time complexity O(n)
     *
     * @return first value
     */
    public T shift() {
        Object[] t = new Object[arr.length - 1];
        System.arraycopy(arr, 1, t, 0, arr.length - 1);
        T tmp = arr[0];
        arr = (T[]) t;
        return tmp;
    }

    /**
     * Move in value
     *
     * @return new array length
     */
    public int unshift(T[] t) {
        T[] tmp = (T[]) Arrays.copyOf(t, t.length + arr.length, arr.getClass());
        System.arraycopy(arr, 0, tmp, t.length, arr.length);
        arr = tmp;
        return arr.length;
    }

    /**
     * Push in value
     */

    /**
     * Jump out value
     *
     * @return last value
     */
    public T pop() {
        T[] tmp = (T[]) Arrays.copyOf(arr, arr.length - 1, arr.getClass());
        T t = arr[arr.length - 1];
        arr = tmp;
        return t;
    }

    /**
     * Push in value
     *
     * @return new array length
     */
    public int push(T[] tmp) {
        arr = (T[]) Arrays.copyOf(arr, arr.length + tmp.length, arr.getClass());
        System.arraycopy(tmp, 0, arr, arr.length, tmp.length);
        return arr.length;
    }

    /**
     * Reverse array, not change origin
     *
     * @return reverse array
     */
    public T[] reverse() {
        T[] tmp = (T[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = arr[tmp.length - 1 - i];
        }
        return tmp;
    }

    /**
     * @return this array
     */
    public T[] toArry() {
        return arr;
    }
}
