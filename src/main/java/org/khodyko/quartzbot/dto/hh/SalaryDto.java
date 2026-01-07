package org.khodyko.quartzbot.dto.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO для представления зарплаты из API hh.ru
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalaryDto {
    private Integer from;
    private Integer to;
    private String currency;
    private Boolean gross;
}

