package org.demo.data.converter;

import org.demo.data.PhoneNumber;
import org.springframework.core.convert.converter.Converter;

public class PhoneNumberToStringConverter implements Converter<PhoneNumber, String>{

    @Override
    public String convert(PhoneNumber source){
        return source.toString();
    }

}
