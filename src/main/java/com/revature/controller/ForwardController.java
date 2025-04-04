package com.revature.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardController {

    @RequestMapping(value = { "/", "/register", "/dashboard" })
    public String forward() {
        return "forward:/index.html";
    }
}
