package com.example.project.controller;

import com.example.project.model.Entity.User;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

import static com.example.project.util.Helper.checkPasswordComplexity;
import static com.example.project.util.Helper.getPasswordHash;
import static com.example.project.util.SessionUtil.*;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final UserService userService;

    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/")
    public ModelAndView viewAccountPage(HttpServletRequest request) {
        if(checkUserAuthorization(request)){
            Long userId = getLongAttrFromSession(request, "userId");
            String userName = userService.getNameById(userId);
            ModelAndView modelAndView = new ModelAndView("account_page");
            modelAndView.addObject("userName", userName);
            return modelAndView;
        }
        return new ModelAndView("redirect:/main");
    }

    @RequestMapping("/password-change")
    public ModelAndView viewPasswordChangePage(HttpServletRequest request) {
        if(checkUserAuthorization(request)){
            Long userId = getLongAttrFromSession(request, "userId");
            String userName = userService.getNameById(userId);
            ModelAndView modelAndView = new ModelAndView("password_change_page");
            modelAndView.addObject("userName", userName);
            return modelAndView;
        }
        return new ModelAndView("redirect:/main");
    }

    @PostMapping("/password-change/do")
    public ModelAndView changeUserPassword(@ModelAttribute(name = "old_password") String oldPassword,
                                           @ModelAttribute(name = "new_password") String newPassword,
                                           @ModelAttribute(name = "password_repetition") String password_repetition,
                                           HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        ModelAndView modelAndView = new ModelAndView("redirect:/account/password-change");
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            oldPassword = getPasswordHash(oldPassword);
            //Проверка старого пароля
            if (Objects.equals(oldPassword, user.getPassword())) {
                //Проверка сложности нового пароля
                if(checkPasswordComplexity(newPassword)) {
                    //Проверка подтверждения пароля
                    if (newPassword.equals(password_repetition)) {
                        newPassword = getPasswordHash(newPassword);
                        userService.changePassword(user.getId(), newPassword);
                        modelAndView = new ModelAndView("redirect:/account/");
                    } else {
                        modelAndView.addObject("result", "Введенные пароли не совпадают");
                    }
                } else {
                    modelAndView.addObject("result", "Пароль недостаточно сложный");
                }
            } else {
                modelAndView.addObject("result", "Неверный старый пароль");
            }
        } else {
            modelAndView = new ModelAndView("redirect:/main");
        }
        return modelAndView;
    }

    @RequestMapping("/logout")
    public ModelAndView logout(HttpServletRequest request) {
        removeAllAttrFromSession(request);
        return new ModelAndView("redirect:/main");
    }

    @RequestMapping("/delete")
    public ModelAndView deleteUser(HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        userService.deleteUser(userId);
        removeAllAttrFromSession(request);
        return new ModelAndView("redirect:/main");
    }
}