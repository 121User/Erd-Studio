package com.example.project.util;

import javax.servlet.http.HttpServletRequest;

public class SessionUtil {
    //Проверка авторизации пользователя
    public static Boolean checkUserAuthorization(HttpServletRequest request) {
        String userId = getAttrFromSession(request, "userId");
        return userId != null;
    }
    //Получение атрибута сессии типа String
    public static String getAttrFromSession(HttpServletRequest request, String name) {
        Object value = request.getSession().getAttribute(name);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    //Получение атрибута сессии типа Long
    public static Long getLongAttrFromSession(HttpServletRequest request, String name) {
        String value = getAttrFromSession(request, name);
        if (value != null) {
            return Long.parseLong(value);
        }
        return (long) 0;
    }

    //Получение атрибута сессии типа Integer
    public static Integer getIntAttrFromSession(HttpServletRequest request, String name) {
        String value = getAttrFromSession(request, name);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    //Установление атрибута в сессию
    public static void setAttrToSession(HttpServletRequest request, String name, Object object) {
        request.getSession().setAttribute(name, object);
    }

    //Удаление всех атрибутов из сессии
    public static void removeAllAttrFromSession(HttpServletRequest request) {
        request.getSession().invalidate();
    }
}
