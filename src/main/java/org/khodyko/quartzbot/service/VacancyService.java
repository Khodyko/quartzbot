package org.khodyko.quartzbot.service;

/**
 * Сервис для поиска и отправки вакансий
 */
public interface VacancyService {
    /**
     * Ищет и отправляет вакансии для всех активных чатов за указанную дату
     *
     * @param date дата поиска в формате YYYY-MM-DD
     */
    void searchAndSendVacancies(String date);
}




