package org.demo.data.formatter;

import org.demo.data.PhoneNumber;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PhoneNunberFactory implements AnnotationFormatterFactory<PhoneNumberA> {

    @Override
    public Set<Class<?>> getFieldTypes() {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(PhoneNumber.class);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Printer<?> getPrinter(PhoneNumberA annotation, Class<?> fieldType) {
        return getFormatter(annotation, fieldType);
    }

    @Override
    public Parser<?> getParser(PhoneNumberA annotation, Class<?> fieldType) {
        return getFormatter(annotation, fieldType);
    }

    private PhoneNumberFormatter getFormatter(PhoneNumberA annotation, Class<?> fieldType) {
        return new PhoneNumberFormatter();
    }

}
