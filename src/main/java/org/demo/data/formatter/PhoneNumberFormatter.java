package org.demo.data.formatter;

import java.text.ParseException;
import java.util.Locale;

import org.demo.data.PhoneNumber;
import org.demo.data.editor.PhoneNumberEditor;
import org.springframework.format.Formatter;

public class PhoneNumberFormatter implements Formatter<PhoneNumber>{

    @Override
    public String print(PhoneNumber object, Locale locale){
        return object.toString();
    }

    @Override
    public PhoneNumber parse(String text, Locale locale) throws ParseException{
        PhoneNumberEditor editor = new PhoneNumberEditor();
        editor.setAsText(text);
        return (PhoneNumber) editor.getValue();
    }

}
