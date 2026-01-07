package org.khodyko.quartzbot.dto.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DTO для представления ответа API hh.ru при поиске вакансий
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VacancyResponseDto {
    private Integer found;
    private Integer pages;
    private Integer perPage;
    private Integer page;
    private List<VacancyItemDto> items;
}

