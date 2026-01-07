package org.khodyko.quartzbot.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.khodyko.quartzbot.config.HhApiConfig;
import org.khodyko.quartzbot.dto.hh.AreaDto;
import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.exception.AreaNotFoundException;
import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.repository.ActiveChatRepository;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.khodyko.quartzbot.service.HhApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActiveChatServiceImpl implements ActiveChatService {
    private static final Logger logger = LoggerFactory.getLogger(ActiveChatServiceImpl.class);

    private final ActiveChatRepository activeChatRepository;
    private final HhApiService hhApiService;
    private final ObjectMapper objectMapper;
    private final HhApiConfig hhApiConfig;

    /**
     * Конструктор для внедрения зависимостей
     *
     * @param activeChatRepository репозиторий для работы с активными чатами
     * @param hhApiService сервис для работы с API hh.ru (ленивая инициализация для разрыва циклической зависимости)
     * @param objectMapper маппер для работы с JSON
     * @param hhApiConfig конфигурация API hh.ru
     */
    @Autowired
    public ActiveChatServiceImpl(
            ActiveChatRepository activeChatRepository,
            @Lazy HhApiService hhApiService,
            ObjectMapper objectMapper,
            HhApiConfig hhApiConfig) {
        this.activeChatRepository = activeChatRepository;
        this.hhApiService = hhApiService;
        this.objectMapper = objectMapper;
        this.hhApiConfig = hhApiConfig;
    }

    @Override
    public ActiveChat updateEnglishByChatId(String chatId, boolean isEnglish) {
        ActiveChat activeChat = activeChatRepository.findByChatId(chatId);

        if (activeChat == null) {
            // Create a new ActiveChat if it doesn't exist
            activeChat = new ActiveChat();
            activeChat.setChatId(chatId);
        }

        // Update the 'english' property
        activeChat.setEnglish(isEnglish);

        // Save the entity (create or update)
        return activeChatRepository.save(activeChat);
    }

    @Override
    public ActiveChat updateJavaByChatId(String chatId, boolean isJava) {
        ActiveChat activeChat = activeChatRepository.findByChatId(chatId);

        if (activeChat == null) {
            // Create a new ActiveChat if it doesn't exist
            activeChat = new ActiveChat();
            activeChat.setChatId(chatId);
        }

        // Update the 'java' property
        activeChat.setJava(isJava);

        // Save the entity (create or update)
        return activeChatRepository.save(activeChat);
    }

    @Override
    public List<ActiveChat> getActiveJavaChats() {
        return activeChatRepository.findByJava(true);
    }

    @Override
    public List<ActiveChat> getActiveEnglishChats() {
        return activeChatRepository.findByEnglish(true);
    }

    @Override
    public ActiveChat setActiveChatTopicByString(String chatId, JavaTopicEnum javaTopicEnum) {
        ActiveChat activeChat = activeChatRepository.findByChatId(chatId);

        activeChat.setJavaTopicEnum(javaTopicEnum);

        // Save the entity (create or update)
        return activeChatRepository.save(activeChat);
    }

    @Override
    public ActiveChat updateVacanciesByChatId(String chatId, boolean isVacancies) {
        ActiveChat activeChat = activeChatRepository.findByChatId(chatId);

        if (activeChat == null) {
            activeChat = new ActiveChat();
            activeChat.setChatId(chatId);
        }

        activeChat.setVacancies(isVacancies);

        // При первом включении вакансий - инициализировать дефолтными регионами, если список пуст
        if (isVacancies && (activeChat.getVacancyAreas() == null || activeChat.getVacancyAreas().isEmpty())) {
            List<String> defaultAreas = getDefaultAreas();
            activeChat.setVacancyAreas(convertAreaIdsToJson(defaultAreas));
        }

        return activeChatRepository.save(activeChat);
    }

    @Override
    public ActiveChat addVacancyAreaByChatId(String chatId, String areaId) {
        ActiveChat activeChat = getOrCreateActiveChat(chatId);
        List<String> areaIds = getVacancyAreasByChatId(chatId);

        // Проверяем, что регион еще не добавлен
        if (!areaIds.contains(areaId)) {
            areaIds.add(areaId);
            activeChat.setVacancyAreas(convertAreaIdsToJson(areaIds));
            return activeChatRepository.save(activeChat);
        }

        return activeChat;
    }

    @Override
    public ActiveChat removeVacancyAreaByChatId(String chatId, String areaId) {
        ActiveChat activeChat = getOrCreateActiveChat(chatId);
        List<String> areaIds = getVacancyAreasByChatId(chatId);

        areaIds.remove(areaId);
        activeChat.setVacancyAreas(convertAreaIdsToJson(areaIds));

        return activeChatRepository.save(activeChat);
    }

    @Override
    public List<String> getVacancyAreasByChatId(String chatId) {
        ActiveChat activeChat = activeChatRepository.findByChatId(chatId);

        if (activeChat == null || activeChat.getVacancyAreas() == null || activeChat.getVacancyAreas().isEmpty()) {
            return getDefaultAreas();
        }

        try {
            return objectMapper.readValue(activeChat.getVacancyAreas(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            logger.error("Ошибка при парсинге JSON для vacancyAreas: {}", e.getMessage(), e);
            return getDefaultAreas();
        }
    }

    @Override
    public ActiveChat setVacancyAreasByChatId(String chatId, List<String> areaIds) {
        ActiveChat activeChat = getOrCreateActiveChat(chatId);
        activeChat.setVacancyAreas(convertAreaIdsToJson(areaIds));
        return activeChatRepository.save(activeChat);
    }

    @Override
    public List<ActiveChat> getActiveVacancyChats() {
        return activeChatRepository.findByVacancies(true);
    }

    @Override
    public List<AreaDto> getVacancyAreasWithNames(String chatId) {
        List<String> areaIds = getVacancyAreasByChatId(chatId);
        List<AreaDto> allAreas = hhApiService.getAreas();

        return areaIds.stream()
                .map(areaId -> allAreas.stream()
                        .filter(area -> area.getId().equals(areaId))
                        .findFirst()
                        .orElse(null))
                .filter(area -> area != null)
                .collect(Collectors.toList());
    }

    @Override
    public ActiveChat addVacancyAreaByName(String chatId, String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Название региона не может быть пустым");
        }

        List<AreaDto> allAreas = hhApiService.getAreas();
        String searchName = areaName.trim().toLowerCase();

        List<AreaDto> matchingAreas = allAreas.stream()
                .filter(area -> area.getName() != null &&
                        area.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());

        if (matchingAreas.isEmpty()) {
            throw new AreaNotFoundException("Регион '" + areaName + "' не найден. Проверьте правильность написания.");
        }

        if (matchingAreas.size() > 1) {
            String foundNames = matchingAreas.stream()
                    .map(AreaDto::getName)
                    .collect(Collectors.joining(", "));
            throw new AreaNotFoundException("Найдено несколько регионов: " + foundNames + ". Пожалуйста, уточните запрос.");
        }

        AreaDto foundArea = matchingAreas.get(0);
        List<String> currentAreas = getVacancyAreasByChatId(chatId);

        if (currentAreas.contains(foundArea.getId())) {
            throw new IllegalArgumentException("Регион '" + foundArea.getName() + "' уже добавлен в список.");
        }

        return addVacancyAreaByChatId(chatId, foundArea.getId());
    }

    @Override
    public ActiveChat removeVacancyAreaByName(String chatId, String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Название региона не может быть пустым");
        }

        List<AreaDto> currentAreasWithNames = getVacancyAreasWithNames(chatId);
        String searchName = areaName.trim().toLowerCase();

        AreaDto foundArea = currentAreasWithNames.stream()
                .filter(area -> area.getName() != null &&
                        area.getName().toLowerCase().equals(searchName))
                .findFirst()
                .orElse(null);

        if (foundArea == null) {
            throw new AreaNotFoundException("Регион '" + areaName + "' не найден в вашем списке.");
        }

        return removeVacancyAreaByChatId(chatId, foundArea.getId());
    }

    /**
     * Получает или создает ActiveChat для указанного chatId
     *
     * @param chatId ID чата
     * @return ActiveChat
     */
    private ActiveChat getOrCreateActiveChat(String chatId) {
        ActiveChat activeChat = activeChatRepository.findByChatId(chatId);
        if (activeChat == null) {
            activeChat = new ActiveChat();
            activeChat.setChatId(chatId);
        }
        return activeChat;
    }

    /**
     * Получает дефолтные регионы из конфигурации
     *
     * @return список ID дефолтных регионов
     */
    private List<String> getDefaultAreas() {
        String defaultAreasStr = hhApiConfig.getDefaultAreas();
        if (defaultAreasStr == null || defaultAreasStr.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(defaultAreasStr.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Конвертирует список ID регионов в JSON строку
     *
     * @param areaIds список ID регионов
     * @return JSON строка
     */
    private String convertAreaIdsToJson(List<String> areaIds) {
        try {
            return objectMapper.writeValueAsString(areaIds);
        } catch (Exception e) {
            logger.error("Ошибка при конвертации areaIds в JSON: {}", e.getMessage(), e);
            return "[]";
        }
    }

    @Override
    public ActiveChat updateMessageThreadId(String chatId, Integer messageThreadId) {
        ActiveChat activeChat = getOrCreateActiveChat(chatId);
        activeChat.setMessageThreadId(messageThreadId);
        return activeChatRepository.save(activeChat);
    }

    @Override
    public ActiveChat getActiveChatByChatId(String chatId) {
        return activeChatRepository.findByChatId(chatId);
    }
}
