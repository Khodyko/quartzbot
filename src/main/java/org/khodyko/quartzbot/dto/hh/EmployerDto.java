package org.khodyko.quartzbot.dto.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для представления работодателя из API hh.ru
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployerDto {
    private String id;
    private String name;
    private String alternateUrl;
}

