package com.example.project.controller;

import com.example.project.model.Entity.User;
import com.example.project.service.EmailService;
import com.example.project.service.UserService;
import com.example.project.util.Helper;
import com.example.project.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

import static com.example.project.util.Helper.getCode;

@RestController
public class StartController {
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public StartController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    //Доступно без авторизации
    @RequestMapping("/main")
    public ModelAndView viewMainPage(HttpServletRequest request) {
        request.getSession().setMaxInactiveInterval(-1); //Указание параметра закрытия сессии: после закрытия браузера
        Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
        String userEmail = userService.getEmailById(userId);

        ModelAndView modelAndView = new ModelAndView("main_page");
        modelAndView.addObject("userEmail", userEmail);
        return modelAndView;
    }

    //Доступно без авторизации
    @RequestMapping("/start")
    public ModelAndView startWork(HttpServletRequest request) {
        ModelAndView modelAndView;
        if(SessionUtil.checkUserAuthorization(request)){
            modelAndView = new ModelAndView("redirect:/diagram/list");
        } else {
            modelAndView = new ModelAndView("redirect:/diagram/0");
        }
        return modelAndView;
    }

    //Доступно без авторизации
    @RequestMapping("/documentation")
    public ModelAndView viewDocumentationPage(HttpServletRequest request) {
        Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
        String userEmail = userService.getEmailById(userId);
        ModelAndView modelAndView = new ModelAndView("documentation_page");
        modelAndView.addObject("userEmail", userEmail);
        return modelAndView;
    }

    @RequestMapping("/authorization")
    public ModelAndView viewAuthorizationPage() {
        return new ModelAndView("authorization_page");
    }

    @PostMapping("/authorization/sign-in")
    public ModelAndView authorization(@ModelAttribute(name = "email") String email,
                                      @ModelAttribute(name = "password") String password,
                                      HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("authorization_page");
        modelAndView.addObject("userEmail", email);
        Optional<User> userOpt = userService.getByEmail(email);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            password = Helper.getPasswordHash(password);
            //Проверка пароля
            if (user.getPassword().equals(password)) {
                Long userId = user.getId();
                SessionUtil.setAttrToSession(request, "userId", userId);

                modelAndView = new ModelAndView("redirect:/diagram/list");
            } else {
                modelAndView.addObject("result", "Неверный пароль");
            }
        } else {
            modelAndView.addObject("result", "Пользователь с таким Email не зарегистрирован");
        }
        return modelAndView;
    }

    @RequestMapping("/registration")
    public ModelAndView viewRegistrationPage() {
        return new ModelAndView("registration_page");
    }

    @PostMapping("/registration/sign-up")
    public ModelAndView registration(@ModelAttribute(name = "email") String email,
                                     @ModelAttribute(name = "password") String password,
                                     @ModelAttribute(name = "password_repetition") String password_repetition,
                                     HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("registration_page");
        modelAndView.addObject("userEmail", email);
        //Проверка существования пользователя в системе
        if (userService.getByEmail(email).isEmpty()) {
            //Проверка сложности пароля
            if(Helper.checkPasswordComplexity(password)) {
                //Проверка подтверждения пароля
                if (password.equals(password_repetition)) {
                    int emailCode = getCode();
                    emailService.sendEmailConfirmationCode(email, emailCode);
                    SessionUtil.setAttrToSession(request, "emailCode", emailCode);
                    SessionUtil.setAttrToSession(request, "userEmail", email);
                    SessionUtil.setAttrToSession(request, "userPassword", password);

                    modelAndView = new ModelAndView("email_confirmation_page");
                    modelAndView.addObject("userEmail", email);

                } else {
                    modelAndView.addObject("result", "Введенные пароли не совпадают");
                }
            } else {
                modelAndView.addObject("result", "Пароль недостаточно сложный");
            }
        } else {
            modelAndView.addObject("result", "Пользователь с таким Email уже существует");
        }
        return modelAndView;
    }

    @RequestMapping("/email-confirmation")
    public ModelAndView viewEmailConfirmationPage() {
        return new ModelAndView("email_confirmation_page");
    }

    @PostMapping("/email-confirmation/confirm")
    public ModelAndView emailConfirmation(@ModelAttribute(name = "code") Integer code,
                                          HttpServletRequest request) {
        String userEmail = SessionUtil.getAttrFromSession(request, "userEmail");
        String userPassword = SessionUtil.getAttrFromSession(request, "userPassword");
        Integer emailCode = SessionUtil.getIntAttrFromSession(request, "emailCode");
        ModelAndView modelAndView;
        //Проверка соответвтвия кода подтверждения электронной почты
        if (emailCode.equals(code)) {
            userPassword = Helper.getPasswordHash(Objects.requireNonNull(userPassword));
            userService.createUser(userEmail, userPassword, "light");
            SessionUtil.removeAllAttrFromSession(request);

            Long userId = userService.getByEmail(userEmail).get().getId();
            SessionUtil.setAttrToSession(request, "userId", userId);
            modelAndView = new ModelAndView("redirect:/diagram/list");
        } else {
            modelAndView = new ModelAndView("email_confirmation_page");
            modelAndView.addObject("userEmail", userEmail);
            modelAndView.addObject("result", "Код подтверждения неверный");
        }
        return modelAndView;
    }

    @RequestMapping("/password-recovery/email")
    public ModelAndView viewPasswordRecoveryEmailPage() {
        return new ModelAndView("password_recovery_email_page");
    }

    @PostMapping("/password-recovery/code")
    public ModelAndView viewPasswordRecoveryCodePage(@ModelAttribute(name = "email") String email,
                                                     HttpServletRequest request) {
        ModelAndView modelAndView;
        Optional<User> userOpt = userService.getByEmail(email);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            int emailCode = getCode();
            emailService.sendPasswordRecoveryCode(email, emailCode);
            SessionUtil.setAttrToSession(request, "userEmail", email);
            SessionUtil.setAttrToSession(request, "emailCode", emailCode);
            modelAndView = new ModelAndView("password_recovery_code_page");
            modelAndView.addObject("userEmail", email);
        } else {
            modelAndView = new ModelAndView("password_recovery_email_page");
            modelAndView.addObject("userEmail", email);
            modelAndView.addObject("result", "Пользователь с таким Email не зарегистрирован");
        }
        return modelAndView;
    }

    @PostMapping("/password-recovery/code/check")
    public ModelAndView checkCode(@ModelAttribute(name = "code") Integer code,
                                  HttpServletRequest request) {
        String userEmail = SessionUtil.getAttrFromSession(request, "userEmail");
        Integer emailCode = SessionUtil.getIntAttrFromSession(request, "emailCode");
        ModelAndView modelAndView;
        //Проверка соответвтвия кода подтверждения смены пароля
        if (emailCode.equals(code)) {
            modelAndView = new ModelAndView("redirect:/password-recovery");
        } else {
            modelAndView = new ModelAndView("password_recovery_code_page");
            modelAndView.addObject("userEmail", userEmail);
            modelAndView.addObject("result", "Код подтверждения неверный");
        }
        return modelAndView;
    }

    @RequestMapping("/password-recovery")
    public ModelAndView viewPasswordRecoveryPage() {
        return new ModelAndView("password_recovery_page");
    }

    @PostMapping("/password-recovery/check")
    public ModelAndView checkPassword(@ModelAttribute(name = "password") String password,
                                      @ModelAttribute(name = "password_repetition") String password_repetition,
                                      HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("password_recovery_page");
        modelAndView.addObject("password", password);
        //Проверка сложности пароля
        if(Helper.checkPasswordComplexity(password)) {
            //Проверка подтверждения пароля
            if (password.equals(password_repetition)) {
                String userEmail = SessionUtil.getAttrFromSession(request, "userEmail");
                password = Helper.getPasswordHash(password);
                userService.changePassword(userEmail, password);
                SessionUtil.removeAllAttrFromSession(request);

                Long userId = userService.getByEmail(userEmail).get().getId();
                SessionUtil.setAttrToSession(request, "userId", userId);
                modelAndView = new ModelAndView("redirect:/diagram/list");
            } else {
                modelAndView.addObject("result", "Введенные пароли не совпадают");
            }
        } else {
            modelAndView.addObject("result", "Пароль недостаточно сложный");
        }
        return modelAndView;
    }
}