package org.demo.data.converter;

import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

public class MultipleConverter implements GenericConverter{

    @Override
    public Set<ConvertiblePair> getConvertibleTypes(){
        return null;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType){
        return null;
    }

}
