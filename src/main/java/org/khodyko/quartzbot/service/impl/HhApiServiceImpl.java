package org.khodyko.quartzbot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.khodyko.quartzbot.config.HhApiConfig;
import org.khodyko.quartzbot.dto.hh.AreaDto;
import org.khodyko.quartzbot.dto.hh.VacancyResponseDto;
import org.khodyko.quartzbot.service.HhApiService;
import org.khodyko.quartzbot.service.SendMeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с API hh.ru
 */
@Service
public class HhApiServiceImpl implements HhApiService {

    private static final Logger logger = LoggerFactory.getLogger(HhApiServiceImpl.class);

    private final RestTemplate restTemplate;
    private final HhApiConfig hhApiConfig;
    private final ObjectMapper objectMapper;
    private final SendMeService sendMeService;

    @Autowired
    public HhApiServiceImpl(HhApiConfig hhApiConfig, ObjectMapper objectMapper, @Lazy SendMeService sendMeService) {
        this.hhApiConfig = hhApiConfig;
        this.objectMapper = objectMapper;
        this.sendMeService = sendMeService;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public VacancyResponseDto searchVacancies(List<String> areaIds, String dateFrom, String dateTo, int page, int perPage) {
        try {
            String url = hhApiConfig.getBaseUrl() + "/vacancies";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("text", hhApiConfig.getSearchText())
                    .queryParam("search_field", "name") // Поиск только в названии вакансии
                    .queryParam("professional_role", hhApiConfig.getProfessionalRoleId())
                    .queryParam("date_from", dateFrom)
                    .queryParam("date_to", dateTo)
                    .queryParam("page", page)
                    .queryParam("per_page", perPage);

            // Добавляем area параметры
            for (String areaId : areaIds) {
                builder.queryParam("area", areaId);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", hhApiConfig.getUserAgent());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<VacancyResponseDto> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    VacancyResponseDto.class
            );

            return response.getBody();
        } catch (Exception e) {
            logger.error("Ошибка при поиске вакансий: {}", e.getMessage(), e);
            sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
            throw new RuntimeException("Ошибка при запросе к API hh.ru", e);
        }
    }

    @Override
    public List<AreaDto> getAreas() {
        try {
            String url = hhApiConfig.getBaseUrl() + "/areas";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", hhApiConfig.getUserAgent());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<AreaDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AreaDto[].class
            );

            // API возвращает дерево регионов, извлекаем все регионы рекурсивно
            List<AreaDto> allAreas = new ArrayList<>();
            if (response.getBody() != null) {
                for (AreaDto area : response.getBody()) {
                    extractAreasRecursively(area, allAreas);
                }
            }

            return allAreas;
        } catch (Exception e) {
            logger.error("Ошибка при получении справочника регионов: {}", e.getMessage(), e);
            sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
            throw new RuntimeException("Ошибка при запросе справочника регионов к API hh.ru", e);
        }
    }

    /**
     * Рекурсивно извлекает все регионы из дерева
     *
     * @param area текущий регион
     * @param result список для накопления всех регионов
     */
    private void extractAreasRecursively(AreaDto area, List<AreaDto> result) {
        if (area == null) {
            return;
        }
        result.add(area);
        if (area.getAreas() != null && !area.getAreas().isEmpty()) {
            for (AreaDto subArea : area.getAreas()) {
                extractAreasRecursively(subArea, result);
            }
        }
    }
}

