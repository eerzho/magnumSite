package kz.iitu.javaee.ilyasProject.controllers;

import kz.iitu.javaee.ilyasProject.entities.Roles;
import kz.iitu.javaee.ilyasProject.entities.Users;
import kz.iitu.javaee.ilyasProject.repositories.RolesRepository;
import kz.iitu.javaee.ilyasProject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@Controller
public class MainController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

//    цукцукцук
    @GetMapping(path = "/")
    public String index(Model model){
        model.addAttribute("classActiveSettingsIndexPage", "active");
        return "index";
    }
//    эти функций для гостя сайта
    @GetMapping(path = "/login")
    public String login(Model model){
        return "guest/login";
    }


    @GetMapping(path = "/registration")
    public String registration (Model model){
        return "guest/registration";
    }

    @PostMapping(value = "/register")
    public String register(
            @RequestParam(name = "user_name") String name,
            @RequestParam(name = "user_email") String email,
            @RequestParam(name = "user_password") String password,
            @RequestParam(name = "user_rePassword") String rePassword,
            Model model){
        model.addAttribute("save_name", name);
        model.addAttribute("save_email", email);
        if (password.length() > 6) {
            if (password.equals(rePassword)) {
                Set<Roles> roles = new HashSet<>();
                Roles r = rolesRepository.findById(2L).orElse(null);
                roles.add(r);
                Users user;
                user = new Users(email, passwordEncoder.encode(password), name, roles);

                userRepository.save(user);
            }
            else {
                model.addAttribute("error", "Password and rePassword not equals");
                return "guest/registration";
            }
        }
        else {
            model.addAttribute("error", "Password length < 6");
            return "guest/registration";
        }
        return "guest/login";
    }

//    функций для пользавателя
    @GetMapping(path = "/profile")
    public String profile(Model model){
        return "profile";
    }

}
