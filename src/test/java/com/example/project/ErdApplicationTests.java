package com.example.project;

import com.example.project.service.DesignThemeService;
import com.example.project.service.DiagramService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ErdApplicationTests {
    private final UserService userService;
    private final DesignThemeService designThemeService;
    private final DiagramService diagramService;

    @Autowired
    public ErdApplicationTests(UserService userService, DesignThemeService designThemeService, DiagramService diagramService) {
        this.userService = userService;
        this.designThemeService = designThemeService;
        this.diagramService = diagramService;
    }

//	@Test
//	void createUserTest() {
//		UserDto userDto = new UserDto("user@mail.com", "111", "light");
//		userService.createUser(userDto);
//		Optional<User> user = userService.getByEmail("user@mail.com");
//		assertNotNull(user.get());
//	}
//
//	@Test
//	void addUserDiagramTest() {
//		DiagramDto diagramDto = new DiagramDto("Новая диаграмма", LocalDateTime.now(),
//				LocalDateTime.now(), "Код диаграммы");
//		Long diagramId = userService.addDiagram("user@mail.com", diagramDto);
//		assertNotNull(diagramId);
////	}
//
//	@Test
//	void changeUserPasswordTest() {
//		userService.changePassword("user@mail.com", "New password");
//		Optional<User> user = userService.getByEmail("user@mail.com");
//		assertEquals("New password", user.get().getPassword());
//	}
//
//	@Test
//	void changeUserDesignThemeTest() {
//		userService.changeDesignTheme("user@mail.com", "dark");
//		Optional<User> user = userService.getByEmail("user@mail.com");
//		assertEquals("dark", user.get().getDesignTheme().getName());
//	}
//
//	@Test
//	void deleteUserTest() {
//		Optional<User> user = userService.getByEmail("user@mail.com");
//		userService.deleteUser(user.get());
//
//		user = userService.getByEmail("user@mail.com");
//		assertEquals(Optional.empty(), user);
//	}
//
//
//	@Test
//	void getCodeTest() {
//		int code = getCode();
//		int length = 0;
//		while (code!=0){
//			code /= 10;
//			length++;
//		}
//		assertEquals(5, length);
//	}
}
