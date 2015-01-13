package org.codeyn.util;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.codeyn.util.yn.StrYn;

public final class Maps{

    public static Map<String, String> toMap(String str, String s, String delim){
        Map<String, String> m = new HashMap<String, String>();
        for(StringTokenizer token = new StringTokenizer(str, delim);token.hasMoreTokens();){
            String[] arr = token.nextToken().split(s);
            m.put(arr[0], arr[1]);
        }
        return m;
    }
    
    public static <K, V> String toString(Map<K, V> m, String s, String delim){
        StringBuilder sb = new StringBuilder();
        for (Iterator<Map.Entry<K, V>> irt = m.entrySet().iterator(); irt.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) irt.next();
            sb.append(entry.getKey()).append(s).append(entry.getValue()).append(delim);
        }
        return sb.toString();
    }
    
    /**
     * Convert as URL parameter, for example:escape=true&resid=13%2524RLLMJ
     */
    public static <K, V> String toUrlParams(Map<K, V> m){
        if(m.isEmpty()) return null;
        StringBuffer sb = new StringBuffer(m.size()<<7);
        for(Iterator<Map.Entry<K, V>> itr = m.entrySet().iterator(); itr.hasNext();){
            Map.Entry<K, V> entry = itr.next();
            if(sb.length() > 0){
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=");
            sb.append(StrYn.encodeURIComponent(StrYn.isNull((String)entry.getValue()) ? "" : (String)entry.getValue()));
        }
        return sb.toString();
    }
    
    public final <K, V> void writeTo(Map<K, V> map, Writer writer, String s, String delim) throws IOException{
        if (map.size() == 0) return;
        if (delim == null) delim = "\r\n";
        boolean appendLn = false;
        Map.Entry<K, V> entry;
        for (Iterator<Map.Entry<K, V>> itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = itr.next();
            if (appendLn) {
                writer.write(delim);
            } else {
                appendLn = true;
            }
            writer.write(entry.getKey().toString());
            writer.write(s);
            Object val = entry.getValue();
            String str = val == null ? "" : val.toString();
            writeQuotedStr(writer, str, s, delim);
        }
    }

    public static final void writeQuotedStr(Writer writer,
                                            String s,
                                            String equal,
                                            String delim) throws IOException{
        if (s == null || s.length() == 0) return;
        if ((s.indexOf(delim) != -1) || (s.indexOf(equal) != -1) || (s.indexOf('\"') != -1) || (s.indexOf('\n') != -1)) {
            writer.write('\"');
            int len = s.length();
            for (int i = 0; i < len; i++) {
                writer.write(s.charAt(i));
            }
            writer.write('\"');
        } else {
            writer.write(s);
        }
    }
    
    
}
