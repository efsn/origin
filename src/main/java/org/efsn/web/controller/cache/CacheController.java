package org.efsn.web.controller.cache;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import template.bean.User;

@Controller
public class CacheController {

    @RequestMapping("/cache.do")
    public String cache(@ModelAttribute("user") User user, Model model) {
        return "";
    }


}
