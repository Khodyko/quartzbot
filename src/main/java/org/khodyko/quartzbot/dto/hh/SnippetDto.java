package org.khodyko.quartzbot.dto.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для представления краткого описания вакансии из API hh.ru
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnippetDto {
    private String requirement;
    private String responsibility;
}

