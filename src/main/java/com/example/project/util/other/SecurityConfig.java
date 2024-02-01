//package com.example.project.util.other;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.core.userdetails.UserDetailsService;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
////    @Autowired
////    private UserDetailsService userDetailsService;
////
////    @Override
////    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
////        auth.userDetailsService(userDetailsService);
////    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                    //Адреса, которые доступны без аутентификации
////                    .antMatchers( "/", "/documentation/**", "/authorization/**",
////                            "/email-confirmation/**", "/password-recovery/**",
////                            "/authorization/{any}/**", "/registration/{any}/**",
////                            "/email-confirmation/confirm/**", "/password-recovery/{any}/**",
////                            "/password-recovery/{any}/{any}/**").permitAll()
////                    .anyRequest().authenticated() //Все остальные адреса требуют аутентификацию
//
//
//                .antMatchers( "/user/**").authenticated()
//                .anyRequest().permitAll() //Все остальные адреса требуют аутентификацию
//
//
//                .and()
//                    .formLogin()
//                        .loginPage("/authorization") //Страница авторизации
//                        .defaultSuccessUrl("/user")
//                        .permitAll()
//                .and()
//                    .logout()
////                        .logoutSuccessUrl("/authorization?logout")
//                        .permitAll();
//    }
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web
//                .ignoring()
//                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
//    }
//}