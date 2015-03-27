package org.demo.multi.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.Valid;

import org.demo.data.PhoneNumber;
import org.demo.data.binder.DataBinderModel;
import org.demo.data.editor.PhoneNumberEditor;
import org.demo.data.validator.DataBinderModelValidator;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DataBindErrorController{
    
    private DataBinderModelValidator validator = new DataBinderModelValidator();
    
    @ModelAttribute("command")
    public DataBinderModel getCommand(){
        DataBinderModel command = new DataBinderModel();
        command.setUsername("Please enter username");
        command.setPassword("Plean enter password");
        return command;
    }
    
    @RequestMapping(value="/ee.do", method=RequestMethod.GET)
    public String show(@Valid @ModelAttribute("command") DataBinderModel command, Model model, BindingResult errors){
        validator.validate(command, errors);
        System.out.println(errors);
        return "error/validator";
    }
    
    @RequestMapping(value="/ee.do", method=RequestMethod.POST)
    public String post(@ModelAttribute("command") DataBinderModel command){
        System.out.println(command);;
        return "error/validator";
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) throws Exception{
        DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
        binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
    }
    
}
