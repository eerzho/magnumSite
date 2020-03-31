package kz.iitu.javaee.ilyasProject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
//    цукцукцук
    @GetMapping(path = "/")
    public String index(Model model){
        model.addAttribute("classActiveSettingsIndexPage", "active");
        return "index";
    }

    @GetMapping(path = "/login")
    public String login(Model model){
        return "annonymous/login";
    }

    @GetMapping(path = "/profile")
    public String profile(Model model){
        return "profile";
    }

}
