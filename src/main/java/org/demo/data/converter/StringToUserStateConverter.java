package org.demo.data.converter;

import org.demo.data.UserState;
import org.springframework.core.convert.converter.Converter;

public class StringToUserStateConverter implements Converter<String, UserState> {

    @Override
    public UserState convert(String source) {
        System.out.println(this.getClass());
        return new UserState(source);
    }

}
