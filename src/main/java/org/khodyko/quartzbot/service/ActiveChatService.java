package org.khodyko.quartzbot.service;

import org.khodyko.quartzbot.dto.hh.AreaDto;
import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.model.ActiveChat;

import java.util.List;

public interface ActiveChatService {
    ActiveChat updateEnglishByChatId(String chatId, boolean isEnglish);

    ActiveChat updateJavaByChatId(String chatId, boolean isJava);

    List<ActiveChat> getActiveJavaChats();

    List<ActiveChat> getActiveEnglishChats();

    ActiveChat setActiveChatTopicByString(String chatId, JavaTopicEnum javaTopicEnum);

    /**
     * Включает/выключает вакансии для чата
     *
     * @param chatId ID чата
     * @param isVacancies флаг включения вакансий
     * @return обновленный ActiveChat
     */
    ActiveChat updateVacanciesByChatId(String chatId, boolean isVacancies);

    /**
     * Добавляет регион в список регионов для поиска вакансий
     *
     * @param chatId ID чата
     * @param areaId ID региона
     * @return обновленный ActiveChat
     */
    ActiveChat addVacancyAreaByChatId(String chatId, String areaId);

    /**
     * Удаляет регион из списка регионов для поиска вакансий
     *
     * @param chatId ID чата
     * @param areaId ID региона
     * @return обновленный ActiveChat
     */
    ActiveChat removeVacancyAreaByChatId(String chatId, String areaId);

    /**
     * Получает список ID регионов для чата (если пусто - возвращает дефолтные)
     *
     * @param chatId ID чата
     * @return список ID регионов
     */
    List<String> getVacancyAreasByChatId(String chatId);

    /**
     * Устанавливает полный список регионов для чата (заменяет текущий)
     *
     * @param chatId ID чата
     * @param areaIds список ID регионов
     * @return обновленный ActiveChat
     */
    ActiveChat setVacancyAreasByChatId(String chatId, List<String> areaIds);

    /**
     * Получает список чатов с включенными вакансиями
     *
     * @return список ActiveChat с включенными вакансиями
     */
    List<ActiveChat> getActiveVacancyChats();

    /**
     * Получает список регионов с их названиями и флагами для отображения
     *
     * @param chatId ID чата
     * @return список регионов с названиями и флагами
     */
    List<AreaDto> getVacancyAreasWithNames(String chatId);

    /**
     * Добавляет регион по названию
     *
     * @param chatId ID чата
     * @param areaName название региона
     * @return обновленный ActiveChat
     * @throws org.khodyko.quartzbot.exception.AreaNotFoundException если регион не найден
     */
    ActiveChat addVacancyAreaByName(String chatId, String areaName);

    /**
     * Удаляет регион по названию
     *
     * @param chatId ID чата
     * @param areaName название региона
     * @return обновленный ActiveChat
     */
    ActiveChat removeVacancyAreaByName(String chatId, String areaName);

    /**
     * Обновляет message_thread_id для чата (используется для форум-групп)
     *
     * @param chatId ID чата
     * @param messageThreadId ID топика форума
     * @return обновленный ActiveChat
     */
    ActiveChat updateMessageThreadId(String chatId, Integer messageThreadId);

    /**
     * Получает ActiveChat по chatId
     *
     * @param chatId ID чата
     * @return ActiveChat или null, если не найден
     */
    ActiveChat getActiveChatByChatId(String chatId);
}
