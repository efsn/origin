package org.demo.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.demo.data.binder.DataBinderModel;
import org.demo.data.editor.PhoneNumberEditor;
import org.demo.data.editor.SchoolInfoEditor;
import org.demo.data.editor.UserStateEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BinderAndValidatorController{
    
    @RequestMapping("/datab.do")
    public String test(DataBinderModel command, Model model){
        System.out.println(command);
        model.addAttribute("dataBinder", command);
        return "binderandvalidator";
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
        binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
        binder.registerCustomEditor(SchoolInfo.class, new SchoolInfoEditor());
        binder.registerCustomEditor(UserState.class, new UserStateEditor());
    }
    
}
