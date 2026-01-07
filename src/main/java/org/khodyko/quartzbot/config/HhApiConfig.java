package org.khodyko.quartzbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Конфигурация для работы с API hh.ru
 */
@Configuration
@Data
@PropertySource("application.properties")
public class HhApiConfig {

    @Value("${hh.api.base-url}")
    private String baseUrl;

    @Value("${hh.api.user-agent}")
    private String userAgent;

    @Value("${hh.vacancies.search-text}")
    private String searchText;

    @Value("${hh.vacancies.professional-role-id}")
    private String professionalRoleId;

    @Value("${hh.vacancies.default-areas}")
    private String defaultAreas;
}

