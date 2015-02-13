package org.demo.multi.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.demo.data.PhoneNumber;
import org.demo.data.editor.PhoneNumberEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class DataBindErrorController extends SimpleFormController{
    
    @Override
    protected ModelAndView showForm(HttpServletRequest request,
                                    HttpServletResponse response,
                                    BindException errors)throws Exception{
        System.out.println(errors);
        request.setAttribute("bool","please entry boolean");
        request.setAttribute("phoneNumber","please entry phone number");
        request.setAttribute("date","please entry date");
        return super.showForm(request, response, errors);
    }
    
    @Override
    protected void doSubmitAction(Object command) throws Exception{
        System.out.println(command);;
    }
    
    @Override
    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws Exception{
        super.initBinder(request, binder);
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
        binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
    }
    
    
}
