package com.example.rateuniversityapplication.controller;



import com.example.rateuniversityapplication.model.*;
import com.example.rateuniversityapplication.repositories.UserRepository;
import com.example.rateuniversityapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;




import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController implements WebMvcConfigurer {


    private final UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;

    }


    @GetMapping("/")
    public String getRegPage(@ModelAttribute("user") User user) {
        return "register";
    }

    @PostMapping("/")
    public String saveUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        // Validate input
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Passwords do not match");
            return "register";
        }



        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt the password

        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "This email is already registered");
            return "register";
        }

        userService.saveUser(user);
        model.addAttribute("message", "Submitted Successfully");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLoginPage(@ModelAttribute("login") Login login) {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("login") Login login, Model model, HttpServletRequest request) {
        String email = login.getUsername();
        String password = login.getPassword();


        User user = userService.findByEmail(email);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Passwords match
            model.addAttribute("user", user);
            request.getSession().setAttribute("user", user);
            return "redirect:/profile/" + user.getId();
        } else {
            // Passwords do not match
            model.addAttribute("error", true);
            return "login";
        }
    }
}