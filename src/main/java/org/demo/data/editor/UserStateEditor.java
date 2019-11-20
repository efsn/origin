package org.demo.data.editor;

import org.codeyn.util.yn.ArrayUtil;
import org.codeyn.util.yn.StrUtil;
import org.demo.data.UserState;

import java.beans.PropertyEditorSupport;

public class UserStateEditor extends PropertyEditorSupport {

    private static final String[] STATES = {"blocked"};

    @Override
    public String getAsText() {
        UserState us = (UserState) getValue();
        return us.getState();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StrUtil.isNull(text)) {
            setValue(null);
        } else {
            int idx = 0;
            if ((idx = ArrayUtil.find(STATES, text)) != -1) {
                UserState us = new UserState(STATES[idx].toUpperCase());
                setValue(us);
            }
        }
    }
}
