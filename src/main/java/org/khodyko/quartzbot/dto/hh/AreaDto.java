package org.khodyko.quartzbot.dto.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DTO для представления региона из API hh.ru
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaDto {
    private String id;
    private String name;
    private List<AreaDto> areas; // Вложенные регионы (для дерева регионов)
}

