package org.demo.data.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class StringToDateConverter implements Converter<String, Date>{

    private String format;
    
    public StringToDateConverter(String format){
        this.format = format;
    }
    
    
    @Override
    public Date convert(String source){
        try {
            return new SimpleDateFormat(this.format).parse(source);
        } catch (ParseException e) {
            return null;
        }
    }

}
