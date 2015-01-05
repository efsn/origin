package org.blue.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordFilter {
    public static Map<Integer, List<String>> EditorCheckFilter(Map<Integer, List<String>> value) {
        // editorCheckNotPass---list(2)
        // ---expertCheckPass list(2)

        Map<Integer, List<String>> fValue = new HashMap<Integer, List<String>>();

        if(value.isEmpty()){
            return value;
        }

        for(Integer i : value.keySet()){
            if(!value.get(i).get(2).startsWith("editorCheckNotPass")
                    && !value.get(i).get(2).endsWith("expertCheckPass")
                    && !"not checked".equals(value.get(i).get(2))){
                fValue.put(i, value.get(i));
            }
        }
        return fValue;
    }

}
