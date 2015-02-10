package org.demo.data.editor;

import java.beans.PropertyEditorSupport;

import org.codeyn.util.yn.StrYn;
import org.demo.data.SchoolInfo;

public class SchoolInfoEditor extends PropertyEditorSupport{

    @Override
    public void setAsText(String text) throws IllegalArgumentException{
        if(StrYn.isNull(text)){
            setValue(null);
        }else{
            SchoolInfo si = new SchoolInfo();
            si.setName(text);
            setValue(si);
        }
    }
    
    @Override
    public String getAsText(){
        Object obj = getValue();
        return obj == null ? "" : obj.toString();
    }
}
