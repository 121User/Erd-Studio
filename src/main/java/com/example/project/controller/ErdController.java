package com.example.project.controller;

import com.example.project.model.Dto.DiagramDto;
import com.example.project.model.Dto.UserDto;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.User;
import com.example.project.service.DiagramService;
import com.example.project.service.EmailService;
import com.example.project.service.UserService;
import com.example.project.util.ExportImportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.project.util.Helper.*;

@RestController
@Slf4j
public class ErdController {
    private UserDto userDto;
    private int emailCode;
    private final EmailService emailService;
    private final UserService userService;
    private final DiagramService diagramService;

    @Autowired
    public ErdController(EmailService emailService, UserService userService, DiagramService diagramService) {
        this.emailService = emailService;
        this.userService = userService;
        this.diagramService = diagramService;
    }

    @RequestMapping("/")
    public ModelAndView getDiagramCodeForImport() {
        return new ModelAndView("main_page");
    }

    @RequestMapping("/documentation")
    public ModelAndView viewDocumentationPage() {
        return new ModelAndView("documentation_page");
    }

    @RequestMapping("/authorization")
    public ModelAndView viewAuthorizationPage() {
        ModelAndView modelAndView;
        if (userDto == null) {
            modelAndView = new ModelAndView("authorization_page");
        } else {
            modelAndView = new ModelAndView("redirect:/user/diagram/list");
        }
        return modelAndView;
    }

    @PostMapping("/authorization/sign-in")
    public ModelAndView authorization(@ModelAttribute(name = "email") String email,
                                      @ModelAttribute(name = "password") String password) {
        ModelAndView modelAndView;

        Optional<User> userOpt = userService.getByEmail(email);
        if (userOpt.isPresent()) {
            if (userOpt.get().getPassword().equals(password)) {
                User user = userOpt.get();
                userDto = new UserDto(user.getEmail(), user.getPassword(), user.getDesignTheme().getName());
                modelAndView = new ModelAndView("redirect:/user/diagram/list");
            } else {
                modelAndView = new ModelAndView("authorization_page");
                modelAndView.addObject("email", email);
                modelAndView.addObject("result", "Неверный пароль");
            }
        } else {
            modelAndView = new ModelAndView("authorization_page");
            modelAndView.addObject("email", email);
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
                                     @ModelAttribute(name = "password_repetition") String password_repetition) {
        ModelAndView modelAndView;
        if (userService.getByEmail(email).isEmpty()) {
            if (password.equals(password_repetition)) {
                userDto = new UserDto(email, password, "light");

                emailCode = getCode();
                emailService.sendEmailConfirmationCode(email, emailCode);

                modelAndView = new ModelAndView("email_confirmation_page");
                modelAndView.addObject("email", email);

            } else {
                modelAndView = new ModelAndView("registration_page");
                modelAndView.addObject("email", email);
                modelAndView.addObject("result", "Введенные пароли не совпадают");
            }
        } else {
            modelAndView = new ModelAndView("registration_page");
            modelAndView.addObject("email", email);
            modelAndView.addObject("result", "Пользователь с таким Email уже существует");
        }
        return modelAndView;
    }

    @RequestMapping("/email-confirmation")
    public ModelAndView viewEmailConfirmationPage() {
        return new ModelAndView("email_confirmation_page");
    }

    @PostMapping("/email-confirmation/confirm")
    public ModelAndView emailConfirmation(@ModelAttribute(name = "code") int code) {
        ModelAndView modelAndView;
        if (code == emailCode) {
            userService.createUser(userDto);
            modelAndView = new ModelAndView("redirect:/user/diagram/list");
        } else {
            modelAndView = new ModelAndView("email_confirmation_page");
            modelAndView.addObject("email", userDto.getEmail());
            modelAndView.addObject("result", "Код подтверждения неверный");
        }
        return modelAndView;
    }

    @RequestMapping("/password-recovery/email")
    public ModelAndView viewPasswordRecoveryEmailPage() {
        return new ModelAndView("password_recovery_email_page");
    }

    @PostMapping("/password-recovery/code")
    public ModelAndView viewPasswordRecoveryCodePage(@ModelAttribute(name = "email") String email) {
        ModelAndView modelAndView;

        Optional<User> userOpt = userService.getByEmail(email);
        if (userOpt.isPresent()) {

            userDto = new UserDto(email, null, null);

            emailCode = getCode();
            emailService.sendPasswordRecoveryCode(email, emailCode);

            modelAndView = new ModelAndView("password_recovery_code_page");
        } else {
            modelAndView = new ModelAndView("password_recovery_email_page");
            modelAndView.addObject("email", email);
            modelAndView.addObject("result", "Пользователь с таким Email не зарегистрирован");
        }
        return modelAndView;
    }

    @PostMapping("/password-recovery/code/check")
    public ModelAndView checkCode(@ModelAttribute(name = "code") int code) {
        ModelAndView modelAndView;
        if (code == emailCode) {
            modelAndView = new ModelAndView("redirect:/password-recovery");
        } else {
            modelAndView = new ModelAndView("password_recovery_code_page");
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
                                      @ModelAttribute(name = "password_repetition") String password_repetition) {
        ModelAndView modelAndView;
        if (password.equals(password_repetition)) {
            userDto.setPassword(password);
            userService.changePassword(userDto.getEmail(), userDto.getPassword());
            userDto.setDesignTheme(userService.getByEmail(userDto.getEmail()).get().getDesignTheme().getName());

            modelAndView = new ModelAndView("redirect:/user/diagram/list");
        } else {
            modelAndView = new ModelAndView("password_recovery_page");
            modelAndView.addObject("password", password);
            modelAndView.addObject("result", "Введенные пароли не совпадают");
        }
        return modelAndView;
    }


    @RequestMapping("/user/account")
    public ModelAndView viewAccountPage() {
        ModelAndView modelAndView = new ModelAndView("account_page");
        modelAndView.addObject("email", userDto.getEmail());
        return modelAndView;
    }

    @RequestMapping("/user/account/password-change")
    public ModelAndView changeUserPasswordPage() {
        return new ModelAndView("password_change_page");
    }

    @PostMapping("/user/account/password-change/do")
    public ModelAndView changeUserPasswordPage(@ModelAttribute(name = "old_password") String oldPassword,
                                               @ModelAttribute(name = "new_password") String newPassword,
                                               @ModelAttribute(name = "password_repetition") String password_repetition) {
        ModelAndView modelAndView;
        if (Objects.equals(oldPassword, userDto.getPassword())) {
            if (newPassword.equals(password_repetition)) {
                userDto.setPassword(newPassword);
                userService.changePassword(userDto.getEmail(), newPassword);

                modelAndView = new ModelAndView("redirect:/user/account");
            } else {
                modelAndView = new ModelAndView("password_change_page");
                modelAndView.addObject("result", "Введенные пароли не совпадают");
            }
        } else {
            modelAndView = new ModelAndView("password_change_page");
            modelAndView.addObject("result", "Неверный старый пароль");
        }
        return modelAndView;
    }

    @RequestMapping("/user/delete")
    public ModelAndView deleteUser() {
        User user = userService.getByEmail(userDto.getEmail()).get();
        userService.deleteUser(user);
        userDto = null;
        return new ModelAndView("redirect:/");
    }

    @RequestMapping("/logout")
    public ModelAndView logout() {
        userDto = null;
        return new ModelAndView("redirect:/");
    }


    @RequestMapping("/user/diagram/list")
    public ModelAndView viewDiagramListPage(@ModelAttribute(name = "old_password") String oldPassword,
                                            @RequestParam(name = "searchText", required = false) String searchText) {
        User user = userService.getByEmail(userDto.getEmail()).get();
        ModelAndView modelAndView = new ModelAndView("diagram_list_page");
        modelAndView.addObject("email", user.getEmail());

        //Поиск по названию диаграммы
        List<Diagram> diagramList = getSortedDiagramList(user.getDiagrams());
        if(searchText != null){
            diagramList = getFilteredDiagramList(user.getDiagrams(), searchText);
        }

        modelAndView.addObject("searchText", searchText);

        if(user.getDiagrams().isEmpty()){
            modelAndView.addObject("listInfo", "Список диаграмм пуст");
        } else {
            modelAndView.addObject("diagramList", diagramList);
        }
        return modelAndView;
    }

    @RequestMapping("/user/diagram/list/delete/{diagramId}")
    public ModelAndView deleteDiagram(@PathVariable(name = "diagramId") Long diagramId) {
        userService.deleteDiagram(diagramId);

        return new ModelAndView("redirect:/user/diagram/list");
    }

    @RequestMapping("/user/diagram/list/import")
    public ModelAndView viewDiagramImportPage() {
        ModelAndView modelAndView= new ModelAndView("diagram_import_page");
        modelAndView.addObject("email", userDto.getEmail());
        return modelAndView;
    }

    @RequestMapping("/user/diagram/list/import/do")
    public ModelAndView importDiagram(@ModelAttribute(name = "fileDiagram") MultipartFile fileDiagram,
                                      @ModelAttribute(name = "codeDiagram") String codeDiagram) throws IOException {
        if(!fileDiagram.isEmpty() || !codeDiagram.equals("")) {
            String code;
            if (!fileDiagram.isEmpty()) {
                StringBuilder contentFile = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileDiagram.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        contentFile.append(line).append("\n");
                    }
                }
                code = ExportImportUtil.getDiagramCodeForImport(contentFile.toString());
            }
            //Импорт из введенного текста
            else {
                code = codeDiagram.replace("\\n", "");
//                code = ExportImportUtil.getDiagramCodeForImport(codeDiagram);
            }
            DiagramDto diagramDto = new DiagramDto("Новая диаграмма", LocalDateTime.now(),
                    LocalDateTime.now(), code);
            Long diagramId = userService.addDiagram(userDto.getEmail(), diagramDto);
            return new ModelAndView("redirect:/user/diagram/" + diagramId);
        }
        return new ModelAndView("redirect:/user/diagram/list/import");
    }

    @RequestMapping("/user/diagram/list/create")
    public ModelAndView viewDiagramCreatePage() {
        return new ModelAndView("diagram_create_page");
    }

    @RequestMapping("/user/diagram/list/create/do")
    public ModelAndView createDiagram(@ModelAttribute(name = "name") String name) {
        DiagramDto diagramDto = new DiagramDto(name, LocalDateTime.now(), null, null);
        Long diagramId = userService.addDiagram(userDto.getEmail(), diagramDto);
        return new ModelAndView("redirect:/user/diagram/" + diagramId);
    }

    @RequestMapping("/user/diagram/{diagramId}")
    public ModelAndView viewDiagramPage(@PathVariable(name = "diagramId") Long diagramId) {
        Diagram diagram = diagramService.getByID(diagramId);
        ModelAndView modelAndView = new ModelAndView("diagram_working_page");
        modelAndView.addObject("userEmail", userDto.getEmail());
        modelAndView.addObject("diagramName", diagram.getName());
        modelAndView.addObject("diagramCode", diagram.getCode());
        boolean theme = userDto.getDesignTheme().equals("dark");
        modelAndView.addObject("designTheme", theme);
        return modelAndView;
    }

    @RequestMapping("/user/diagram/{diagramId}/save")
    public ModelAndView saveChanges(@PathVariable(name = "diagramId") Long diagramId,
                                    @RequestParam(name = "designTheme", required = false) String designTheme,
                                    @RequestParam(name = "diagramName", required = false) String diagramName,
                                    @RequestParam(name = "diagramCode", required = false) String diagramCode) {
        Diagram diagram = diagramService.getByID(diagramId);
        diagramService.changeName(diagram, diagramName, userService.getByEmail(userDto.getEmail()).get());
        diagramService.changeCode(diagram, diagramCode);
        if(designTheme != null){
            userDto.setDesignTheme(designTheme);
            userService.changeDesignTheme(userDto.getEmail(), designTheme);
        }

        return new ModelAndView("redirect:/user/diagram/" + diagramId);
    }

    @RequestMapping("/user/diagram/{diagramId}/export")
    public ModelAndView exportCode(@PathVariable(name = "diagramId") Long diagramId,
                                   @RequestParam(name = "type") String type,
                                   HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Diagram diagram = diagramService.getByID(diagramId);
        String headerKey = "Content-Disposition";

        String encodedFileName = URLEncoder.encode(diagram.getName() + ".sql", StandardCharsets.UTF_8.toString());
        String headerValue = "attachment; filename=" + encodedFileName;
        response.setHeader(headerKey, headerValue);

        ServletOutputStream outputStream = response.getOutputStream();
        String text = "";
        if(type.equals("mssql")) {
            text = ExportImportUtil.convertToMsSqlServer(diagram.getCode());
        } else if (type.equals("postgresql")){
            text = ExportImportUtil.convertToPostgresql(diagram.getCode());
        }

        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
        outputStream.close();

        return new ModelAndView("redirect:/user/diagram/" + diagramId);
    }
}

