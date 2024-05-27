package com.example.project.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
    //Получение кода для отправки на email
    public static int getEmailCode(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static int getEmailCode() {
        return getEmailCode(10000, 100000);
    }

    //Проверка сложности пароля
    public static boolean checkPasswordComplexity(String password) {
        if (password.length() >= 12) {
            Pattern lowerCaseLetter = Pattern.compile("[a-zа-я]");
            Pattern upperCaseLetter = Pattern.compile("[A-ZА-Я]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

            Matcher hasLowerCaseLetter = lowerCaseLetter.matcher(password);
            Matcher hasUpperCaseLetter = upperCaseLetter.matcher(password);
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);

            return hasLowerCaseLetter.find() && hasUpperCaseLetter.find() && hasDigit.find() && hasSpecial.find();
        }
        return false;
    }

    //Хеширование пароля (фиксированная длина - 32)
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

    //Шифрование URL-адреса
    public static String encryptUrl(String url) {
        byte[] encodedBytes = Base64.getEncoder().encode(url.getBytes(StandardCharsets.UTF_8));
        return new String(encodedBytes);
    }

    //Расшифровка URL-адреса
    public static String decryptUrl(String encryptedUrl) {
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedUrl.getBytes());
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}