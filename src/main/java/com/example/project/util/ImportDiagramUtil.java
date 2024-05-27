package com.example.project.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImportDiagramUtil {
    //Получение кода диаграммы для иморта
    public static String getDiagramCodeForImport(String sqlCode) {
        if (sqlCode.contains("[")) {
            return getCodeFromMsSqlServer(sqlCode);
        } else if (sqlCode.contains("\"")) {
            return getCodeFromPostgresql(sqlCode);
        } else {
            return sqlCode;
        }
    }

    //Получение кода диаграммы из Ms Sql Server скрипта
    private static String getCodeFromMsSqlServer(String sqlCode) {
        String result = sqlCode.replace("PRIMARY KEY", "PK")
                .replaceAll("CREATE TABLE ", "").replace("NOT NULL", "not null")
                .replaceAll("]\s*\\(\s*\n", "{\n").replaceAll("\n\\)", "\n}\n")
                .replaceAll("\\[", "").replaceAll("\nGO", "")
                .replaceAll("]", "").replaceAll(",", "")
                .replaceAll(" NULL", "");
        //Обработка связей
        for (String string : sqlCode.split("\n")) {
            if (string.contains("ALTER TABLE")) {
                String tableName = "";
                String attrName = "";
                Matcher matcher = Pattern.compile("ALTER TABLE \\[(.*?)] ADD").matcher(string);
                if (matcher.find()) {
                    tableName = matcher.group().replace("ALTER TABLE [", "")
                            .replace("] ADD", "");
                }
                matcher = Pattern.compile("KEY \\(\\[(.*?)]\\) REFERENCES").matcher(string);
                if (matcher.find()) {
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
        result = result.replaceAll("\n+", "\n");
        return result;
    }

    //Получение кода диаграммы из PostgreSQL скрипта
    private static String getCodeFromPostgresql(String sqlCode) {
        String result = sqlCode.replace("PRIMARY KEY", "PK")
                .replaceAll("CREATE TABLE ", "").replace("NOT NULL", "not null")
                .replaceAll("\"\s+\\(\s*\n", "{\n").replaceAll("\n\\);", "\n}\n")
                .replaceAll("\"", "").replaceAll("\nGO", "")
                .replaceAll(",", "").replaceAll(" \\(", "(");
        //Обработка связей
        for (String string : sqlCode.split("\n")) {
            if (string.contains("ALTER TABLE")) {
                String tableName = "";
                String attrName = "";
                Matcher matcher = Pattern.compile("ALTER TABLE \"(.*?)\" ADD").matcher(string);
                if (matcher.find()) {
                    tableName = matcher.group().replace("ALTER TABLE \"", "")
                            .replace("\" ADD", "");
                }
                matcher = Pattern.compile("KEY \\(\"(.*?)\"\\) REFERENCES").matcher(string);
                if (matcher.find()) {
                    attrName = matcher.group().replace("KEY (\"", "")
                            .replace("\") REFERENCES", "");
                }
                String ref = "ref: " + string.replace("ALTER TABLE \"", "")
                        .replaceFirst("\" ADD FOREIGN KEY \\(\"(.*?)\"\\) REFERENCES \"", " < ")
                        .replaceFirst("\" \\(\"(.*?)\"\\)", "");

                result = replaceTextByRef(result, tableName, attrName, ref).replace(";", "");
            }
        }
        result = addSquareBrackets(result);
        result = result.replaceAll("\n+", "\n");
        return result;
    }


    //Изменение связи сущности
    private static String replaceTextByRef(String text, String tableName, String attrName, String ref) {
        Matcher matcher = Pattern.compile(tableName + "\\{(.*?)}", Pattern.DOTALL).matcher(text);
        if (matcher.find()) {
            String table = matcher.group();
            for (String tableStr : table.split("\n")) {
                String trimStr = tableStr.trim();
                if (trimStr.startsWith(attrName)) {
                    if (trimStr.contains("PK") && trimStr.contains("not null")) {
                        ref = ref.replaceFirst(" < ", " - "); //Изменение типа связи
                        text = text.replaceFirst(trimStr, trimStr
                                .replaceFirst("PK(.*?)not null", "PK, " + ref + ", not null"));
                    } else if (trimStr.contains("PK")) {
                        ref = ref.replaceFirst(" < ", " - "); //Изменение типа связи
                        text = text.replaceFirst(trimStr, trimStr
                                .replaceFirst("PK", "PK, " + ref));
                    } else if (trimStr.contains("not null")) {
                        text = text.replaceFirst(trimStr, trimStr
                                .replaceFirst("not null", ref + ", not null"));
                    } else {
                        text = text.replaceFirst(trimStr, trimStr + " " + ref);
                    }
                    break;
                }
            }
        }
        return text;
    }

    //Обход по строкам добавление квадратных скобок, удаление пустых строк
    private static String addSquareBrackets(String text) {
        StringBuilder result = new StringBuilder();
        for (String string : text.split("\n")) {
            if (string.contains("PK")) {
                result.append(string.replace("PK", "[PK")).append("]");
            } else if (string.contains("ref: ")) {
                result.append(string.replace("ref: ", "[ref: ")).append("]");
            } else if (string.contains("not null")) {
                result.append(string.replace("not null", "[not null")).append("]");
            } else if (!string.equals("") && !string.contains("ALTER TABLE")) {
                result.append(string);
            }
            //Перевод строки
            if (!string.equals("")) {
                result.append("\n");
            }
        }
        return result.toString();
    }
}