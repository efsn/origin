package org.codeyn.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public final class Maps<K, V>{

    public static Map<String, String> toMap(String str, String s, String delim){
        Map<String, String> m = new HashMap<String, String>();
        for(StringTokenizer token = new StringTokenizer(str, delim);token.hasMoreTokens();){
            String[] arr = token.nextToken().split(s);
            m.put(arr[0], arr[1]);
        }
        return m;
    }
    
    public static <K, V> String  toString(Map<K, V> m, String s, String delim){
        StringBuilder sb = new StringBuilder();
        for (Iterator<Map.Entry<K, V>> irt = m.entrySet().iterator(); irt.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) irt.next();
            sb.append(entry.getKey()).append(s).append(entry.getValue()).append(delim);
        }
        return sb.toString();
    }
    
}
