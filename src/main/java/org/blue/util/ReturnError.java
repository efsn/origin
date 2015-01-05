package org.blue.util;

public class ReturnError {
    public static boolean isLength(String str) {
        if(null == str){
            return false;
        }
        else{
            if(str.length() >= 5 && str.length() <= 16){
                return true;
            }
            else{
                return false;
            }
        }
    }

    public static boolean isEquals(String str1, String str2) {
        if(null != str1){
            if(str1.equals(str2)){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}
