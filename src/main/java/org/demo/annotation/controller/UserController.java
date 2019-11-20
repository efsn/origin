package org.demo.annotation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/ann/create.do")
public class UserController {

    @RequestMapping(params = "_tag!=create", method = RequestMethod.GET, headers = "Content-Type=application/json")
    public String showForm() {
        return "create";
    }

    @RequestMapping(params = "_tag=create", method = RequestMethod.POST, headers = "Content-Type=application/json")
    public String create() {
        return "create";
    }
}
