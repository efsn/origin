package org.demo.data;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.demo.data.editor.PhoneNumberEditor;
import org.demo.data.editor.SchoolInfoEditor;
import org.demo.data.editor.UserStateEditor;
import org.demo.data.formatter.FormatterModel;
import org.demo.data.formatter.PhoneNumberA;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BinderAndValidatorController{
    
    @RequestMapping("/datab.do")
    public String test(@ModelAttribute("dataBinder") FormatterModel command, Model model){
        System.out.println(command);
//        model.addAttribute("dataBinder", command);
        return "binderandvalidator";
    }
    
    @RequestMapping("/datac.do")
    public String test2(HttpServletResponse resp,
                        @PhoneNumberA PhoneNumber phoneNumber,
                        @DateTimeFormat(pattern="yyyy-MM-ddHH:mm:ss") Date date) throws IOException{
        System.out.println(phoneNumber);
        System.out.println(date);
        
        resp.setContentType("application/xml;charset=utf8");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xml += "<user><username>chan</username></user>";
        resp.getWriter().write(xml);
        
        return null;
    }
    
//    @InitBinder
    public void initBinder(WebDataBinder binder){
        DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
        binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
        binder.registerCustomEditor(SchoolInfo.class, new SchoolInfoEditor());
        binder.registerCustomEditor(UserState.class, new UserStateEditor());
    }
    
}
