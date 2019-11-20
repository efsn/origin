package org.demo.data.validator;

import org.demo.data.binder.DataBinderModel;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class DataBinderModelValidator implements Validator {

    public static final String U_P = "^[a-zA-Z]\\w{4,9}$";
    public static final String P_P = "^[a-zA-Z0-9]{5,10}$";
    private static Set<String> forbidden = new HashSet<String>();

    static {
        forbidden.addAll(Arrays.asList("admin", "fuck"));
    }

    public static void main(String[] args) {
        System.out.println(Pattern.matches("^[a-zA-Z]\\w{4,9}$", "aaa12_"));
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == DataBinderModel.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "username", "username.not.empty");
        DataBinderModel dbm = (DataBinderModel) target;
        if (!Pattern.matches(U_P, dbm.getUsername())) {
            errors.rejectValue("username", "username.not.illegal");
        } else {
            for (String u : forbidden) {
                if (dbm.getUsername().contains(u)) {
                    errors.rejectValue("username", "username.not.valid", "contains forbidden word");
                }
            }
        }

        if (!Pattern.matches(P_P, dbm.getPassword())) {
            errors.rejectValue("password", "password.not.illegal", "password is invalid");
        }
    }

}
