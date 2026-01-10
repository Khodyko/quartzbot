package org.khodyko.quartzbot.service.impl;

import org.khodyko.quartzbot.dto.hh.VacancyItemDto;
import org.khodyko.quartzbot.dto.hh.VacancyResponseDto;
import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.khodyko.quartzbot.service.HhApiService;
import org.khodyko.quartzbot.service.SendMeService;
import org.khodyko.quartzbot.service.VacancyMessageFormatter;
import org.khodyko.quartzbot.service.VacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.khodyko.quartzbot.bots.quartz.QuartzMessageBot;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для поиска и отправки вакансий
 */
@Service
public class VacancyServiceImpl implements VacancyService {

    private static final Logger logger = LoggerFactory.getLogger(VacancyServiceImpl.class);
    private static final int PER_PAGE = 100;
    private static final int DELAY_BETWEEN_MESSAGES_MS = 150;
    private static final int DELAY_BETWEEN_10_MESSAGES_MS = 30000;
    private static final int DELAY_BETWEEN_PAGES_MS = 500;

    private final ActiveChatService activeChatService;
    private final HhApiService hhApiService;
    private final VacancyMessageFormatter vacancyMessageFormatter;
    private final SendMeService sendMeService;
    private final QuartzMessageBot quartzMessageBot;

    @Autowired
    public VacancyServiceImpl(
            ActiveChatService activeChatService,
            HhApiService hhApiService,
            VacancyMessageFormatter vacancyMessageFormatter,
            @Lazy SendMeService sendMeService,
            @Lazy QuartzMessageBot quartzMessageBot) {
        this.activeChatService = activeChatService;
        this.hhApiService = hhApiService;
        this.vacancyMessageFormatter = vacancyMessageFormatter;
        this.sendMeService = sendMeService;
        this.quartzMessageBot = quartzMessageBot;
    }

    @Override
    public void searchAndSendVacancies(String date) {
        try {
            List<ActiveChat> vacancyChats = activeChatService.getActiveVacancyChats();
            logger.info("Начинаем отправку вакансий за {} для {} чатов", date, vacancyChats.size());

            for (ActiveChat chat : vacancyChats) {
                try {
                    processChatVacancies(chat, date);
                } catch (Exception e) {
                    logger.error("Ошибка при обработке чата {}: {}", chat.getChatId(), e.getMessage(), e);
                    sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .collect(Collectors.toList()));
                }
            }

            logger.info("Завершена отправка вакансий за {}", date);
        } catch (Exception e) {
            logger.error("Критическая ошибка при отправке вакансий: {}", e.getMessage(), e);
            sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Обрабатывает поиск и отправку вакансий для одного чата
     *
     * @param chat активный чат
     * @param date дата поиска в формате YYYY-MM-DD
     */
    private void processChatVacancies(ActiveChat chat, String date) {
        String chatId = chat.getChatId();
        List<String> areaIds = activeChatService.getVacancyAreasByChatId(chatId);

        if (areaIds.isEmpty()) {
            logger.warn("Список регионов пуст для чата {}", chatId);
            return;
        }

        int totalFound = 0;
        int totalSent = 0;
        int page = 0;
        int totalPages = 1;

        // Обрабатываем все страницы результатов
        do {
            VacancyResponseDto response = hhApiService.searchVacancies(
                    areaIds,
                    date,
                    date,
                    page,
                    PER_PAGE
            );

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                break;
            }

            if (page == 0) {
                totalFound = response.getFound() != null ? response.getFound() : 0;
                totalPages = response.getPages() != null ? response.getPages() : 1;
                logger.info("Найдено вакансий для чата {}: {}", chatId, totalFound);
            }

            // Отправляем каждую вакансию отдельным сообщением
            for (VacancyItemDto vacancy : response.getItems()) {
                try {
                    // Пропускаем вакансии с JavaScript
                    if (shouldSkipVacancy(vacancy)) {
                        logger.debug("Пропущена вакансия {} - содержит JavaScript", vacancy.getId());
                        continue;
                    }

                    String formattedMessage = vacancyMessageFormatter.formatVacancy(vacancy);
                    sendMessageToChat(formattedMessage, chatId);
                    totalSent++;
                    if(totalSent%10==0){
                        Thread.sleep(DELAY_BETWEEN_10_MESSAGES_MS);
                    }

                    // Небольшая задержка между сообщениями для избежания rate limiting
                    Thread.sleep(DELAY_BETWEEN_MESSAGES_MS);
                } catch (Exception e) {
                    logger.error("Ошибка при отправке вакансии {} в чат {}: {}", vacancy.getId(), chatId, e.getMessage(), e);
                }
            }

            page++;

            // Задержка в пол секунды между запросами страниц
            if (page < totalPages) {
                try {
                    Thread.sleep(DELAY_BETWEEN_PAGES_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Прервана задержка между запросами страниц");
                    break;
                }
            }
        } while (page < totalPages);

        // Отправляем итоговое сообщение
        if (totalSent > 0) {
            String summaryMessage = vacancyMessageFormatter.formatSummary(totalFound, date);
            sendMessageToChatAndPin(summaryMessage, chatId);
        } else {
            String noVacanciesMessage = String.format("❌ Вакансий за %s не найдено", date);
            sendMessageToChat(noVacanciesMessage, chatId);
        }

        logger.info("Отправлено вакансий в чат {}: {} из {}", chatId, totalSent, totalFound);
    }

    /**
     * Проверяет, нужно ли пропустить вакансию.
     * Вакансия подходит, если содержит слово "java", которое не является частью "javascript"
     * (т.е. не "java" + "script" или "java " + "script" или "java-" + "script")
     *
     * @param vacancy вакансия для проверки
     * @return true, если вакансию нужно пропустить
     */
    private boolean shouldSkipVacancy(VacancyItemDto vacancy) {
        if (vacancy == null || vacancy.getName() == null) {
            return true;
        }

        String name = vacancy.getName().toLowerCase();

        // Ищем слово "java" в названии
        int javaIndex = name.indexOf("java");
        if (javaIndex == -1) {
            // Если нет слова "java" вообще - пропускаем
            return true;
        }

        // Проверяем, что после "java" не следует "script" (сразу, после пробела или тире)
        int afterJavaIndex = javaIndex + 4; // "java" = 4 символа

        if (afterJavaIndex < name.length()) {
            String afterJava = name.substring(afterJavaIndex);

            // Проверяем паттерны: "javascript", "java script", "java-script", "java_script"
            if (afterJava.startsWith("script") ||
                    afterJava.startsWith(" script") ||
                    afterJava.startsWith("-script") ||
                    afterJava.startsWith("_script")) {
                // Это "javascript" - пропускаем
                return true;
            }
        }

        // Если "java" найден и не является частью "javascript" - вакансия подходит
        return false;
    }

    /**
     * Отправляет сообщение в чат без закрепления с поддержкой топиков форума
     *
     * @param text текст сообщения
     * @param chatId ID чата
     */
    private void sendMessageToChat(String text, String chatId) {
        try {
            if (chatId != null) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(text);

                // Получаем ActiveChat для проверки наличия message_thread_id (для форум-групп)
                ActiveChat activeChat = activeChatService.getActiveChatByChatId(chatId);
                if (activeChat != null && activeChat.getMessageThreadId() != null) {
                    // Если есть сохраненный thread_id, используем его (это форум-группа)
                    message.setMessageThreadId(activeChat.getMessageThreadId());
                }

                quartzMessageBot.execute(message);
            }
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения в чат {}: {}", chatId, e.getMessage(), e);
            sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Отправляет сообщение в чат и закрепляет его с поддержкой топиков форума
     *
     * @param text текст сообщения
     * @param chatId ID чата
     */
    private void sendMessageToChatAndPin(String text, String chatId) {
        try {
            if (chatId != null) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(text);

                // Получаем ActiveChat для проверки наличия message_thread_id (для форум-групп)
                ActiveChat activeChat = activeChatService.getActiveChatByChatId(chatId);
                if (activeChat != null && activeChat.getMessageThreadId() != null) {
                    // Если есть сохраненный thread_id, используем его (это форум-группа)
                    message.setMessageThreadId(activeChat.getMessageThreadId());
                }

                // Отправляем сообщение и получаем его для закрепления
                Message sentMessage = quartzMessageBot.execute(message);
                if (sentMessage != null && sentMessage.getMessageId() != null) {
                    Long msgId = sentMessage.getMessageId().longValue();
                    PinChatMessage pinChatMessage = new PinChatMessage(chatId, Math.toIntExact(msgId));
                    quartzMessageBot.execute(pinChatMessage);
                    logger.debug("Сообщение закреплено в чате {}: {}", chatId, msgId);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при отправке и закреплении сообщения в чат {}: {}", chatId, e.getMessage(), e);
            sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }
    }
}

