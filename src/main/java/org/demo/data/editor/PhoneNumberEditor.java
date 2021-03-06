package org.demo.data.editor;

import org.codeyn.util.yn.StrUtil;
import org.demo.data.PhoneNumber;

import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberEditor extends PropertyEditorSupport {

    private static final Pattern pattern = Pattern.compile("^(\\d{3,4})-(\\d{7,8})$");

    @Override
    public String getAsText() {
        PhoneNumber pn = (PhoneNumber) getValue();
        return pn == null ? "" : pn.getAreaCode() + "-" + pn.getPhoneNum();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StrUtil.isNull(text)) setValue(null);
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            PhoneNumber pn = new PhoneNumber();
            pn.setAreaCode(matcher.group(1));
            pn.setPhoneNum(matcher.group(2));
            setValue(pn);
        } else {
            throw new IllegalArgumentException(String.format("Type convert fail, need format[010-12345678], however it's [%s]", text));
        }
    }

}
