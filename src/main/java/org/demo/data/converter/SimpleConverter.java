package org.demo.data.converter;

import org.demo.data.PhoneNumber;
import org.demo.data.editor.PhoneNumberEditor;
import org.springframework.core.convert.converter.Converter;

public class SimpleConverter implements Converter<String, PhoneNumber>{

    private static PhoneNumberEditor editor = new PhoneNumberEditor();
    
    @Override
    public PhoneNumber convert(String source){
        editor.setAsText(source);
        return (PhoneNumber) editor.getValue();
    }

}
