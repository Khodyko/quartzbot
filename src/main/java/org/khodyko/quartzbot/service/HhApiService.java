package org.khodyko.quartzbot.service;

import org.khodyko.quartzbot.dto.hh.AreaDto;
import org.khodyko.quartzbot.dto.hh.VacancyResponseDto;

import java.util.List;

/**
 * Сервис для работы с API hh.ru
 */
public interface HhApiService {
    /**
     * Получает список вакансий по заданным параметрам
     *
     * @param areaIds список ID регионов
     * @param dateFrom дата начала поиска (YYYY-MM-DD)
     * @param dateTo дата окончания поиска (YYYY-MM-DD)
     * @param page номер страницы
     * @param perPage количество элементов на странице
     * @return список вакансий
     */
    VacancyResponseDto searchVacancies(List<String> areaIds, String dateFrom, String dateTo, int page, int perPage);

    /**
     * Получает справочник регионов
     *
     * @return список регионов
     */
    List<AreaDto> getAreas();
}

