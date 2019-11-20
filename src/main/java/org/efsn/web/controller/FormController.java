package org.efsn.web.controller;

import org.codeyn.util.yn.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import template.bean.User;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/form.do")
public class FormController {

    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("user") User user) {
        System.out.println(user.getUsername() + ":" + user.getPassword());
        return "redirect:/command.do";
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView show(@ModelAttribute("user") User user, Model model) {
        return new ModelAndView("register").addObject(user);
    }

    @ModelAttribute("user")
    protected User getUser(@RequestParam(value = "username", required = false) String username) {
        User user = new User();
        user.setUsername(StrUtil.isNull(username) ? "Please enter your username" : username);
        return user;
    }

    @ModelAttribute("address")
    protected List<String> referenceData() {
        return Arrays.asList("NewYork", "HongKong");
    }

    public ModelAndView onCancel(User user) throws Exception {
        submit(user);
        return null;
    }

}
