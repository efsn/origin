package org.demo.data.controller;

import org.demo.data.binder.DataBinderModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {

    @RequestMapping("/error.do")
    public ModelAndView handle(@ModelAttribute("command") DataBinderModel command, Model model, BindingResult errors) throws Exception {
        errors.reject("username.not.empty");
        errors.reject("username.not.empty1", "Username not empty1");
        errors.reject("username.length.error", new Object[]{6, 10}, "Username length should six to ten");
        return new ModelAndView(getErrorView(), errors.getModel());
    }

    public String getErrorView() {
        return "error/error";
    }

}
