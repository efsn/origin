package org.blue.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.blue.sys.factory.PryFactory;
import org.blue.sys.vo.Author;
import org.blue.sys.vo.Editor;
import org.blue.sys.vo.Essay;
import org.blue.sys.vo.Expert;

public class VerifyInfo {
    public static boolean isValid(List<String> list) throws Exception {
        Map<String, List<String>> map = PryFactory.getPryQueryDao().verifyAuthorInfo(list);

        if(map.containsKey(list.get(0))){
            if(!map.get(list.get(0)).contains(list.get(1))){
                return false;
            }
            if(!map.get(list.get(0)).contains(list.get(2))){
                return false;
            }
            if(!map.get(list.get(0)).contains(list.get(3))){
                return false;
            }
            if(!map.get(list.get(0)).contains(list.get(4))){
                return false;
            }
            return true;
        }
        else{
            return false;
        }
    }

    public static boolean isRepeat(Author author) throws Exception {
        Map<String, String> map = PryFactory.getPryQueryDao().getAuthor();
        if(map.containsKey(author.getAuthorPname())){
            return true;
        }
        return false;
    }

    public static boolean isRepeat(Editor editor) throws Exception {
        Map<String, String> map = PryFactory.getPryQueryDao().getEditor();
        if(map.containsKey(editor.getEditorPname())){
            return true;
        }
        return false;
    }

    public static boolean isRepeat(Expert expert) throws Exception {
        Map<String, String> map = PryFactory.getPryQueryDao().getExpert();
        if(map.containsKey(expert.getExpert_pname())){
            return true;
        }
        return false;
    }

    public static boolean isRepeat(Essay essay) throws Exception {
        Map<String, String> map = PryFactory.getPryQueryDao().getEssay();
        if(map.containsKey(essay.getEssayName())){
            return true;
        }
        return false;
    }

    public static boolean isNotRepeat(String typeName) throws SQLException,
            IOException, ClassNotFoundException {
        Map<Integer, List<String>> map = PryFactory.getPryQueryAllDao()
                .getEssayType();
        for(Iterator<List<String>> itr = map.values().iterator(); itr.hasNext();){
            if(true == itr.next().contains(typeName)){
                return false;
            }
        }
        return true;
    }

}
