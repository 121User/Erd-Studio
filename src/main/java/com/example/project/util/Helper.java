package com.example.project.util;

import com.example.project.model.Entity.Diagram;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
    //Получение кода для отправки на email
    public static int getCode(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static int getCode() {
        return getCode(10000, 100000);
    }

    //Получение фильтрованного списка диаграмм по тексту поиска
    public static List<Diagram> getFilteredDiagramList(List<Diagram> diagramList, String searchText) {
        List<Diagram> result = new ArrayList<>();
        for (Diagram d : diagramList) {
            if (d.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                result.add(d);
            }
        }
        return result;
    }

    //Сортировка списка диаграмм
    public static List<Diagram> getSortedDiagramList(List<Diagram> diagramList) {
        List<Diagram> result = new ArrayList<>();
        for (String modifiedDate : getDiagramModifiedDates(diagramList)) {
            for (Diagram d : diagramList) {
                if (d.getModifiedDate().equals(modifiedDate)) {
                    result.add(d);
                    break;
                }
            }
        }
        return result;
    }

    //Получение списка дат изменения диаграмм из списка
    public static List<String> getDiagramModifiedDates(List<Diagram> diagramList) {
        List<String> result = new ArrayList<>();
        for (Diagram d : diagramList) {
            result.add(d.getModifiedDate());
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }

    //Проверка сложности пароля
    public static boolean checkPasswordComplexity(String password) {
        if(password.length() >= 12)
        {
            Pattern lowerCaseLetter = Pattern.compile("[a-zа-я]");
            Pattern upperCaseLetter = Pattern.compile("[A-ZА-Я]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

            Matcher hasLowerCaseLetter = lowerCaseLetter.matcher(password);
            Matcher hasUpperCaseLetter = upperCaseLetter.matcher(password);
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);

            return hasLowerCaseLetter.find() && hasUpperCaseLetter.find() && hasDigit.find() && hasSpecial.find();
        }
        return false;
    }

    //Хэширование пароля
    public static String getPasswordHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Передача байтов пароля
            md.update(password.getBytes());
            //Получение байтов хэша пароля с байтами в десятичном формате
            byte[] bytesPasswordHash = md.digest();
            //Преобразование байтов в шестнадцатиричный формат
            StringBuilder stringBuilder = new StringBuilder();
            for (byte byteHash : bytesPasswordHash) {
                stringBuilder.append(Integer.toString((byteHash & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
