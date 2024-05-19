package com.example.project.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ImportUtil {
    //Получение кода диаграммы для иморта
    public static String getDiagramCodeForImport(String sqlCode){
        if(sqlCode.contains("[")){
            return getCodeFromMsSqlServer(sqlCode);
        } else if (sqlCode.contains("\"")){
            return getCodeFromPostgresql(sqlCode);
        } else {
            return sqlCode;
        }
    }

    //Получение кода диаграммы из Ms Sql Server скрипта
    private static String getCodeFromMsSqlServer(String sqlCode){
        String result = sqlCode.replace("PRIMARY KEY", "PK")
                .replaceAll("CREATE TABLE ", "").replace("NOT NULL", "not null")
                .replace("] (", " {").replaceAll("\n\\)", "\n}\n")
                .replaceAll("\\[", "").replaceAll("\nGO", "")
                .replaceAll("]", "").replaceAll(",", "");
        //Связи
        for (String string : sqlCode.split("\n")) {
            if (string.contains("ALTER TABLE")) {
                String tableName = "";
                String attrName = "";
                Matcher matcher = Pattern.compile("ALTER TABLE \\[(.*?)] ADD").matcher(string);
                if(matcher.find()) {
                    tableName = matcher.group().replace("ALTER TABLE [", "")
                            .replace("] ADD", "");
                }
                matcher = Pattern.compile("KEY \\(\\[(.*?)]\\) REFERENCES").matcher(string);
                if(matcher.find()) {
                    attrName = matcher.group().replace("KEY ([", "")
                            .replace("]) REFERENCES", "");
                }
                String ref = "ref: " + string.replace("ALTER TABLE [", "")
                        .replaceFirst("] ADD FOREIGN KEY \\(\\[(.*?)]\\) REFERENCES \\[", " - ")
                        .replaceFirst("] \\(\\[(.*?)]\\)", "");

                //Поиск связи по сущности
                result = replaceTextByRef(result, tableName, attrName, ref);
            }
        }
        result = addSquareBrackets(result);
        return result;
    }

    //Получение кода диаграммы из PostgreSQL скрипта
    private static String getCodeFromPostgresql(String sqlCode){
        String result = sqlCode.replace("PRIMARY KEY", "PK")
                .replaceAll("CREATE TABLE ", "").replace("NOT NULL", "not null")
                .replace("\" (", " {").replaceAll("\n\\);", "\n}\n")
                .replaceAll("\"", "").replaceAll("\nGO", "")
                .replaceAll(",", "");
        //Связи
        for (String string : sqlCode.split("\n")) {
            if (string.contains("ALTER TABLE")) {
                String tableName = "";
                String attrName = "";
                Matcher matcher = Pattern.compile("ALTER TABLE \"(.*?)\" ADD").matcher(string);
                if(matcher.find()) {
                    tableName = matcher.group().replace("ALTER TABLE \"", "")
                            .replace("\" ADD", "");
                }
                matcher = Pattern.compile("KEY \\(\"(.*?)\"\\) REFERENCES").matcher(string);
                if(matcher.find()) {
                    attrName = matcher.group().replace("KEY (\"", "")
                            .replace("\") REFERENCES", "");
                }
                String ref = "ref: " + string.replace("ALTER TABLE \"", "")
                        .replaceFirst("\" ADD FOREIGN KEY \\(\"(.*?)\"\\) REFERENCES \"", " - ")
                        .replaceFirst("\" \\(\"(.*?)\"\\)", "");

                result = replaceTextByRef(result, tableName, attrName, ref).replace(";", "");
            }
        }
        result = addSquareBrackets(result);
        return result;
    }


    //Поиск связи по сущности
    private static String replaceTextByRef(String text, String tableName, String attrName, String ref){
        Matcher matcher = Pattern.compile(tableName + "(.*?)}", Pattern.DOTALL).matcher(text);
        if(matcher.find()){
            String table = matcher.group();
            for (String tableStr : table.split("\n")) {
                if (tableStr.trim().startsWith(attrName)) {
                    if (tableStr.contains("PK") && tableStr.contains("not null")) {
                        text = text.replaceFirst(tableStr.trim(), tableStr.trim()
                                .replaceFirst("PK(.*?)not null", "PK, " + ref + ", not null"));
                    } else if (tableStr.contains("PK")) {
                        text = text.replaceFirst(tableStr.trim(), tableStr.trim()
                                .replaceFirst("PK", "PK, " + ref));
                    } else if (tableStr.contains("not null")) {
                        text = text.replaceFirst(tableStr.trim(), tableStr.trim()
                                .replaceFirst("not null", ref + ", not null"));
                    }
                }
            }
        }
        return text;
    }

    //Обход по строкам добавление квадратных скобок, удаление пустых строк
    private static String addSquareBrackets(String text){
        StringBuilder result = new StringBuilder();
        for (String string : text.split("\n")) {
            if (string.contains("PK")) {
                result.append(string.replace("PK", "[PK")).append("]");
            } else if (string.contains("ref: ")) {
                result.append(string.replace("ref: ", "[ref: ")).append("]");
            } else if (string.contains("not null")) {
                result.append(string.replace("not null", "[not null")).append("]");
            } else if (!string.equals("") && !string.contains("ALTER TABLE")){
                result.append(string);
            }
            //Перевод строки
            if (!string.equals("")){
                result.append("\n");
            }
        }
        return result.toString();
    }
}
