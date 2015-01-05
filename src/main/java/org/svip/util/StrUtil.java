package org.svip.util;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * String static tools
 *
 * @author Blues
 */
public final class StrUtil{
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String concat(String[] arr, String circle, String append){
        if(arr == null || arr.length < 1){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        String c = "";
        if(!isNull(circle)){
           c = circle;
        }
        for(String str : arr){
            sb.append(c).append(str).append(c).append(append);
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String substring(String str, String delim){
        return substring(str, delim, false);
    }

    public static String substring(String str, String delim, boolean isLast){
        if(isNull(delim) || isNull(str)){
            return str;
        }
        if(isLast){
            return str.substring(str.lastIndexOf(delim) + 1);
        }else{
            return str.substring(0, str.indexOf(delim));
        }
    }

    public static boolean isNull(String str){
        if(str == null || str.length() < 1){
            return true;
        }
        return false;
    }

    public static String add(String str, int idx, String sub){
        if(isNull(sub)){
            return str;
        }
        if(isNull(str)){
            return sub;
        }
        if(idx > str.length() - 1){
            return str;
        }
        StringBuffer sb = new StringBuffer(str.substring(0, idx));
        sb.append(sub).append(str.substring(idx));
        return sb.toString();
    }

    /**
     * Generate database name
     */
    public static String getDbName(String beanName){
        if(StrUtil.isNull(beanName)){
            return "NAME";
        }
        String[] lowers = Pattern.compile("[A-Z]+").split(beanName);
        if(lowers.length < 2){
            return beanName.toUpperCase();
        }
        int lastIdx = 0;
        for(String lower : lowers){
            int idx = beanName.indexOf(lower, lastIdx) + lower.length();
            beanName = StrUtil.add(beanName, idx, "_");
            lastIdx = idx;
        }
        return beanName.toUpperCase();
    }

    public static String getDbName(String[] beanNames){
        StringBuffer sb = new StringBuffer();
        String[] arr = new String[beanNames.length];
        for(String str : beanNames){
            String dbName = getDbName(str);
            if(dbName.startsWith("_")){
                sb.append(dbName);
            }else{
                sb.append("_").append(dbName);
            }
        }
        return sb.toString();
    }

    public static boolean equals(String str1, String str2){
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    public static String MD5(String md) throws UnsupportedEncodingException{
        return MD5(md.getBytes(DEFAULT_CHARSET));
    }

    public static String MD5(String md, String charset) throws UnsupportedEncodingException{
        return MD5(md.getBytes(charset));
    }

    /**
     * byte0 >>> 4 & 0xf 为取字节的高4位,byte0 & 0xf 为取字节的低4位
     * 因为hexDigits[]只有16个字符,所以每4位就可以对应一个字符(2的4次方),一个字节可以获得两个字符
     * 所以一个对字符串进行加密后获得的byte[],转换为字符串后可以得到两倍length的字符串
     */
    private final static String MD5(byte[] md){
        int j = md.length;
        char[] chr = new char[j * 2];
        int k = 0;
        for(int i = 0; i < j; i++){
            byte bt = md[i];
            chr[k++] = HEX_DIGITS[bt >>> 4 & 0xf];
            chr[k++] = HEX_DIGITS[bt & 0xf];
        }
        return String.valueOf(chr);
    }

}
