package com.example.project.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExportImportUtil {

    //Экспорт
    //Преобразование кода диаграммы для Ms Sql Server
    public static String convertToMsSqlServer(String code){
        String refs = getMsSqlServerRefs(code);
        String result1 = code.replace("pk", "PRIMARY KEY")
                .replace("PK", "PRIMARY KEY").replaceAll("\\[", "")
                .replace("not null", "NOT NULL").replace("}", ")\nGO\n")
                .replaceAll("ref:(.*?)[,\\]]", "").replaceAll(", ", " ")
                .replaceAll("]", "").replaceAll("( *)\\{", "] (");

        StringBuilder result = new StringBuilder();
        for(String string: result1.split("\n")){
            if(string.endsWith("(")){
                result.append("CREATE TABLE [").append(string).append("\n");
            } else if(!string.endsWith(")") && !string.contains("GO") && !string.equals("")){
                String attr = string.trim().split("\\s")[0];
                String str = string.replace(attr, "["+attr+"]").replaceFirst("\\s++$", "");
                result.append(str).append(",").append("\n");
            }
            else {
                result.append(string).append("\n");
            }
        }
        return result + refs;
    }

    //Получение строк создания связей для Ms Sql Server
    private static String getMsSqlServerRefs(String code) {
        StringBuilder refs = new StringBuilder();
        String[] strings = code.split("\n");
        for (String string : strings) {
            string = string.trim();
            Matcher matcher = Pattern.compile("ref: (.*?)[,\\]]").matcher(string);
            if (matcher.find()) {
                String[] matchers = matcher.group().split("\\s");
                matchers[3] = matchers[3].replace(",","").replace("]", "");
                refs.append("\nALTER TABLE [").append(matchers[1]).append("] ADD FOREIGN KEY ([")
                        .append(string.split("\\s+")[0]).append("]) REFERENCES [")
                        .append(matchers[3]).append("] ([").append(getPkRef(code, matchers[3]))
                        .append("])\nGO\n");
            }
        }
        refs = new StringBuilder(refs.toString()
                .replaceAll(",", ""));

        return refs.toString();
    }

    //Преобразование кода диаграммы для PostgreSQL
    public static String convertToPostgresql(String code){
        String refs = getPostgresqlRefs(code);
        String result1 = code.replace("pk", "PRIMARY KEY")
                .replace("PK", "PRIMARY KEY").replaceAll("\\[", "")
                .replace("not null", "NOT NULL").replace("}", ");\n")
                .replaceAll("ref:(.*?)[,\\]]", "").replaceAll(", ", " ")
                .replaceAll("]", "").replaceAll("( *)\\{", "\" (");

        StringBuilder result = new StringBuilder();
        for(String string: result1.split("\n")){
            if(string.endsWith("(")){
                result.append("CREATE TABLE \"").append(string).append("\n");
            } else if(!string.contains(")") && !string.equals("")){
                String attr = string.trim().split("\\s")[0];
                String str = string.replace(attr, "\""+attr+"\"").replaceFirst("\\s++$", "");
                result.append(str).append(",").append("\n");
            }
            else {
                result.append(string).append("\n");
            }
        }
        return result + refs;
    }

    //Получение строк создания связей для PostgreSQL
    private static String getPostgresqlRefs(String code) {
        StringBuilder refs = new StringBuilder();
        String[] strings = code.split("\n");
        for (String string : strings) {
            string = string.trim();
            Matcher matcher = Pattern.compile("ref: (.*?)[,\\]]").matcher(string);
            if (matcher.find()) {
                String[] matchers = matcher.group().split("\\s");
                matchers[3] = matchers[3].replace(",","").replace("]", "");
                refs.append("\nALTER TABLE \"").append(matchers[1]).append("\" ADD FOREIGN KEY (\"")
                        .append(string.split("\\s+")[0]).append("\") REFERENCES \"")
                        .append(matchers[3]).append("\" (\"").append(getPkRef(code, matchers[3]))
                        .append("\");\n");
            }
        }
        refs = new StringBuilder(refs.toString()
                .replaceAll(",", ""));

        return refs.toString();
    }

    //Получение названия первичного ключа второй сущности для связи
    private static String getPkRef(String code, String entity){
        String[] strings = code.split("\n");
        for(int i = 0; i < strings.length; i++){
            if(strings[i].startsWith(entity)){
                return strings[i + 1].trim().split("\\s")[0];
            }
        }
        return null;
    }

    //Импорт
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
