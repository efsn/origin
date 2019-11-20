package org.demo.data.converter;

import org.demo.data.PhoneNumber;
import org.demo.data.editor.PhoneNumberEditor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

public class StringToPhoneNumberConverter implements Converter<String, PhoneNumber> {

    private static PhoneNumberEditor editor = new PhoneNumberEditor();

    public static void main(String[] args) {
        DefaultConversionService cs = new DefaultConversionService();
        cs.addConverter(new StringToPhoneNumberConverter());

        System.out.println(cs.convert("017-88888888", PhoneNumber.class));

    }

    @Override
    public PhoneNumber convert(String source) {
        editor.setAsText(source);
        return (PhoneNumber) editor.getValue();
    }


}
