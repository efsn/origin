package org.demo.data.controller;

import org.demo.data.PhoneNumber;
import org.demo.data.SchoolInfo;
import org.demo.data.UserState;
import org.demo.data.binder.DataBinderModel;
import org.demo.data.editor.PhoneNumberEditor;
import org.demo.data.editor.SchoolInfoEditor;
import org.demo.data.editor.UserStateEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class DataBinderController {

    @RequestMapping("/data.do")
    public ModelAndView handle(@ModelAttribute("command") DataBinderModel command, Model model) throws Exception {
        return new ModelAndView("data").addObject("data", command);
    }

    @ModelAttribute("command")
    public DataBinderModel getModel() {
        DataBinderModel model = new DataBinderModel();
        model.setUsername("xxx");
        return model;
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
        binder.registerCustomEditor(PhoneNumber.class, new PhoneNumberEditor());
        binder.registerCustomEditor(SchoolInfo.class, new SchoolInfoEditor());
        binder.registerCustomEditor(UserState.class, new UserStateEditor());
    }

}
