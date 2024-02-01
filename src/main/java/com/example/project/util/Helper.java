package com.example.project.util;

import com.example.project.model.Entity.Diagram;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Helper {
    public static int getCode(int min, int max){
        Random random = new Random();
        return random.nextInt(max-min) + min;
    }
    public static int getCode(){
        return getCode(10000, 100000);
    }

    public static List<Diagram> getFilteredDiagramList(List<Diagram> diagramList, String searchText){
        List<Diagram> result = new ArrayList<>();
        for(Diagram d: diagramList){
            if(d.getName().toLowerCase().startsWith(searchText.toLowerCase())){
                result.add(d);
            }
        }
        return result;
    }

    public static List<Diagram> getSortedDiagramList(List<Diagram> diagramList){
        List<Diagram> result = new ArrayList<>();
        for(String modifiedDate : getDiagramModifiedDates(diagramList)){
            for(Diagram d: diagramList){
                if(d.getModifiedDate().equals(modifiedDate)){
                    result.add(d);
                    break;
                }
            }
        }
        return result;
    }
    public static List<String> getDiagramModifiedDates(List<Diagram> diagramList){
        List<String> result = new ArrayList<>();
        for(Diagram d: diagramList){
            result.add(d.getModifiedDate());
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }
}
