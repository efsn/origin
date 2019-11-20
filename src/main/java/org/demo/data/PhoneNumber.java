package org.demo.data;

import org.demo.data.editor.PhoneNumberEditor;

public class PhoneNumber {

    private String areaCode;
    private String phoneNum;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    @Override
    public String toString() {
        PhoneNumberEditor x = new PhoneNumberEditor();
        x.setValue(this);
        return x.getAsText();
    }

}
