package org.demo.data.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.demo.data.PhoneNumber;
import org.demo.data.SchoolInfo;
import org.demo.data.UserState;
import org.demo.data.binder.DataBinderModel;
import org.demo.data.editor.PhoneNumberEditor;
import org.demo.data.editor.SchoolInfoEditor;
import org.demo.data.editor.UserStateEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

public class DataBinderController extends AbstractCommandController{
    
    public DataBinderController(){
        setCommandClass(DataBinderModel.class);
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Object command,
                                  BindException errors)throws Exception{
        return new ModelAndView("data").addObject("data", command);
    }
    
    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception{
        super.initBinder(request, binder);
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
//        binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
//        binder.registerCustomEditor(SchoolInfo.class, new SchoolInfoEditor());
//        binder.registerCustomEditor(UserState.class, new UserStateEditor());
    }

}
