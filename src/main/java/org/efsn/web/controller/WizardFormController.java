package org.efsn.web.controller;

import org.codeyn.util.yn.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import template.bean.User;

import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/wizard.do")
@SessionAttributes
public class WizardFormController {

    private String cancelView = "redirect:/hello.do";
    private String finishView = "redirect:/form.do";

    public String getCancelView() {
        return cancelView;
    }

    public String getFinishView() {
        return finishView;
    }

    @ModelAttribute
    public User getUser() {
        User user = new User();
        user.setUsername("Please enter your name");
        user.setPassword("Please enter your password");
        return user;
    }

    @ModelAttribute("address")
    public List<String> getAddress() {
        return Arrays.asList("NewYork", "HongKong");
    }

    @ModelAttribute("page")
    public String getPages(@RequestParam(value = "_target", required = false) String target,
                           @RequestParam(value = "above", required = false) String above) {
        List<String> strList = Arrays.asList("wizard/base", "wizard/school", "wizard/work");
        int page = StrUtil.isNull(above) ? StrUtil.isNull(target) ? 0 : Integer.parseInt(target)
                : StrUtil.isNull(target) ? 0 : Integer.parseInt(target) - 2;
        return strList.get(page < strList.size() ? page : strList.size() - 1);
    }

    @RequestMapping()
    public ModelAndView wizard(@ModelAttribute User user, Model model) throws Exception {
        return new ModelAndView((String) model.asMap().get("page")).addObject(user);
    }

    @RequestMapping(params = "_cancel")
    public ModelAndView cancel() throws Exception {
        return new ModelAndView(getCancelView());
    }

    @RequestMapping(params = "_finish")
    public ModelAndView finish() throws Exception {
        return new ModelAndView(getFinishView());
    }

    public void validatePage() {
    }

    public void postProcessPage() throws Exception {
    }

}
