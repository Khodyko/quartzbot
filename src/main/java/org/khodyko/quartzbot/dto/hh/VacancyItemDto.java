package org.khodyko.quartzbot.dto.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO для представления одной вакансии из API hh.ru
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VacancyItemDto {
    private String id;
    private String name;
    
    @JsonProperty("alternate_url")
    private String alternateUrl;
    private String publishedAt;
    private AreaDto area;
    private EmployerDto employer;
    private SalaryDto salary;
    private SnippetDto snippet;
    private String description;
}

