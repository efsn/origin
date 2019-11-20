package org.demo.data.editor;

import org.codeyn.util.yn.StrUtil;
import org.demo.data.SchoolInfo;

import java.beans.PropertyEditorSupport;

public class SchoolInfoEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        Object obj = getValue();
        return obj == null ? "" : obj.toString();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StrUtil.isNull(text)) {
            setValue(null);
        } else {
            SchoolInfo si = new SchoolInfo();
            si.setName(text);
            setValue(si);
        }
    }
}
