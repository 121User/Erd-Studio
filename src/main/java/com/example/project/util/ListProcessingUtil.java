package com.example.project.util;

import com.example.project.model.Dto.DiagramHistoryOutputDto;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.DiagramHistory;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.GroupUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListProcessingUtil {

    //Обработка списков диаграмм
    //Фильтрация списка диаграмм по тексту поиска
    public static List<Diagram> filterDiagramListBySearch(List<Diagram> diagramList, String searchText) {
        List<Diagram> result = new ArrayList<>();
        for (Diagram d : diagramList) {
            if (d.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                result.add(d);
            }
        }
        return result;
    }

    //Фильтрация списка диаграмм по владельцу
    public static List<Diagram> filterDiagramListByOwner(List<Diagram> diagramList, Long userId) {
        List<Diagram> result = new ArrayList<>();
        for (Diagram d : diagramList) {
            if (d.getOwner().getId().equals(userId)) {
                result.add(d);
            }
        }
        return result;
    }

    //Сортировка списка диаграмм по дате изменения
    public static List<Diagram> sortDiagramListByModDate(List<Diagram> diagramList) {
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
    private static List<String> getDiagramModifiedDates(List<Diagram> diagramList) {
        List<String> result = new ArrayList<>();
        for (Diagram d : diagramList) {
            result.add(d.getModifiedDate());
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }

    //Сортировка списка диаграмм по дате изменения
    public static List<DiagramHistoryOutputDto> sortDiagramHistoryListByModDate(List<DiagramHistoryOutputDto> DiagramHistoryOutputDtoList) {
        List<DiagramHistoryOutputDto> result = new ArrayList<>();
        for (String modifiedDate : getDiagramHistoryModifiedDates(DiagramHistoryOutputDtoList)) {
            for (DiagramHistoryOutputDto dh : DiagramHistoryOutputDtoList) {
                if (dh.getModifiedDate().equals(modifiedDate)) {
                    result.add(dh);
                    break;
                }
            }
        }
        return result;
    }

    //Получение списка дат изменения диаграмм из списка
    private static List<String> getDiagramHistoryModifiedDates(List<DiagramHistoryOutputDto> DiagramHistoryOutputDtoList) {
        List<String> result = new ArrayList<>();
        for (DiagramHistoryOutputDto dh : DiagramHistoryOutputDtoList) {
            result.add(dh.getModifiedDate());
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }

    //Обработка списков групп
    //Фильтрация списка групп по тексту поиска
    public static List<Group> filterGroupListBySearch(List<Group> groupList, String searchText) {
        List<Group> result = new ArrayList<>();
        for (Group g : groupList) {
            if (g.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                result.add(g);
            }
        }
        return result;
    }

    //Сортировка списка групп по дате создания
    public static List<Group> sortGroupListByCreateDate(List<Group> groupList) {
        List<Group> result = new ArrayList<>();
        for (String creationDate : getGroupCreationDates(groupList)) {
            for (Group g : groupList) {
                if (g.getCreationDate().equals(creationDate)) {
                    result.add(g);
                    break;
                }
            }
        }
        return result;
    }

    //Получение списка дат создания групп из списка
    private static List<String> getGroupCreationDates(List<Group> groupList) {
        List<String> result = new ArrayList<>();
        for (Group g : groupList) {
            result.add(g.getCreationDate());
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }

    //Обработка списков пользователей группы
    //Фильтрация списка диаграмм по тексту поиска
    public static List<GroupUser> filterGroupUserListBySearch(List<GroupUser> groupUserList, String searchText) {
        List<GroupUser> result = new ArrayList<>();
        for (GroupUser gu : groupUserList) {
            if (gu.getUser().getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                result.add(gu);
            }
        }
        return result;
    }

    //Сортировка списка пользователей по дате входа в группу
    public static List<GroupUser> sortGroupUserListByEntryDate(List<GroupUser> groupUserList) {
        List<GroupUser> result = new ArrayList<>();
        for (String entryDate : getGroupUserEntryDates(groupUserList)) {
            for (GroupUser gu : groupUserList) {
                if (gu.getEntryDate().equals(entryDate)) {
                    result.add(gu);
                    break;
                }
            }
        }
        return result;
    }

    //Получение списка дат входа пользователей из списка
    private static List<String> getGroupUserEntryDates(List<GroupUser> groupUserList) {
        List<String> result = new ArrayList<>();
        for (GroupUser gr : groupUserList) {
            result.add(gr.getEntryDate());
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }
}