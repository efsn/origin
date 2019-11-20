package org.demo.data.binder;

import org.demo.data.PhoneNumber;
import org.demo.data.SchoolInfo;
import org.demo.data.UserState;
import org.demo.data.editor.PhoneNumberEditor;
import org.demo.data.editor.SchoolInfoEditor;
import org.demo.data.editor.UserStateEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BinderInit implements WebBindingInitializer {

    @Override
    public void initBinder(WebDataBinder binder, WebRequest request) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
        binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
        binder.registerCustomEditor(SchoolInfo.class, new SchoolInfoEditor());
        binder.registerCustomEditor(UserState.class, new UserStateEditor());
    }

}
