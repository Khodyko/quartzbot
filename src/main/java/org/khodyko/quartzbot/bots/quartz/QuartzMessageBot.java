package org.khodyko.quartzbot.bots.quartz;

import org.khodyko.quartzbot.config.BotConfig;
import org.khodyko.quartzbot.dto.hh.AreaDto;
import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.exception.AreaNotFoundException;
import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.model.JavaMessage;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.khodyko.quartzbot.service.JavaMessageService;
import org.khodyko.quartzbot.service.EnglishMessageService;
import org.khodyko.quartzbot.model.EnglishMessage;
import org.khodyko.quartzbot.service.SendMeService;
import org.khodyko.quartzbot.service.VacancyMessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuartzMessageBot extends TelegramLongPollingBot {

    private static final String ENGLISH_ON_BTN = "englishOn";
    private static final String ENGLISH_OFF_BTN = "englishOff";
    private static final String JAVA_ON_BTN = "javaOn";
    private static final String JAVA_OFF_BTN = "javaOff";

    private static final String GET_RANDOM_JAVA_QUESTION_BTN= "getRandomJavaQuestionBtn";
    private static final String GET_RANDOM_ENGLISH_QUESTION_BTN= "getRandomEnglishQuestionBtn";
    private static final String JAVA_TOPIC_SET_COMMAND = "java_topic_set_bot";
    private static final String JAVA_TOPIC_GET_ALL_COMMAND = "java_topic_get_all";

    private static final String VACANCIES_ON_BTN = "vacanciesOn";
    private static final String VACANCIES_OFF_BTN = "vacanciesOff";
    private static final String VACANCIES_SET_AREAS_BTN = "vacanciesSetAreas";
    private static final String VACANCIES_SHOW_AREAS_BTN = "vacanciesShowAreas";
    private static final String VACANCIES_REMOVE_AREA_BTN = "vacanciesRemoveArea";

    private static final String VACANCIES_AREA_ADD_COMMAND = "vacancies_area_add";
    private static final String VACANCIES_AREA_REMOVE_COMMAND = "vacancies_area_remove";
    private static final String VACANCIES_AREA_LIST_COMMAND = "vacancies_area_list";

    Logger logger = LoggerFactory.getLogger(QuartzMessageBot.class);

    private final BotConfig botConfig;
    private ActiveChatService activeChatService;
    private JavaMessageService javaMessageService;
    private EnglishMessageService englishMessageService;

    private SendMeService sendMeService;
    private VacancyMessageFormatter vacancyMessageFormatter;

    @Autowired
    public QuartzMessageBot(BotConfig botConfig, ActiveChatService activeChatService, JavaMessageService javaMessageService, EnglishMessageService englishMessageService, @Lazy SendMeService sendMeService, VacancyMessageFormatter vacancyMessageFormatter) {
        this.botConfig = botConfig;
        this.activeChatService = activeChatService;
        this.javaMessageService = javaMessageService;
        this.englishMessageService = englishMessageService;
        this.sendMeService = sendMeService;
        this.vacancyMessageFormatter = vacancyMessageFormatter;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleIncomingMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleIncomingMessage(Update update) {
        try {
            Long chatId = update.getMessage().getChatId();
            String msgTxt = update.getMessage().getText();
            
            // Сохраняем message_thread_id если сообщение из топика форума
            Integer messageThreadId = update.getMessage().getMessageThreadId();
            if (messageThreadId != null) {
                // Проверяем, что это форум-группа (supergroup с включенными топиками)
                if (update.getMessage().getChat().getIsForum()) {
                    activeChatService.updateMessageThreadId(String.valueOf(chatId), messageThreadId);
                    logger.debug("Сохранен message_thread_id {} для чата {}", messageThreadId, chatId);
                }
            }
            
            if (msgTxt != null) {
                if (msgTxt.equals("@" + botConfig.getBotName())) {
                    handleButtonGetQuestion(chatId);
                } else if (msgTxt.equals("@" + botConfig.getBotName() + "_chat_settings")) {
                    handleButtonChatSettings(chatId);
                } else if (msgTxt.startsWith(JAVA_TOPIC_SET_COMMAND)) {
                    String topicForChangeStr = msgTxt.substring(JAVA_TOPIC_SET_COMMAND.length()).trim();
                    JavaTopicEnum javaTopicEnum = JavaTopicEnum.findByString(topicForChangeStr);
                    ActiveChat activeChat = activeChatService.setActiveChatTopicByString(String.valueOf(chatId), javaTopicEnum);
                    if (activeChat != null) {
                        sendStandardMsg(String.valueOf(chatId), "Установлен топик: " + activeChat.getJavaTopicEnum().getNameOfTopic());
                    } else {
                        sendStandardMsg(String.valueOf(chatId), "Произошла ошибка установки топика");
                    }
                } else if (msgTxt.equals(JAVA_TOPIC_GET_ALL_COMMAND)) {
                    sendStandardMsg(String.valueOf(chatId), JavaTopicEnum.toStringTopicNames());
                } else if (msgTxt.startsWith(VACANCIES_AREA_ADD_COMMAND)) {
                    handleVacancyAreaAdd(chatId, msgTxt);
                } else if (msgTxt.startsWith(VACANCIES_AREA_REMOVE_COMMAND)) {
                    handleVacancyAreaRemove(chatId, msgTxt);
                } else if (msgTxt.equals(VACANCIES_AREA_LIST_COMMAND)) {
                    handleVacancyAreaList(chatId);
                }
            }
        } catch (Exception e) {
            sendMeService.sendMe(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()));
        }
    }

    private void handleButtonGetQuestion(Long chatId){
        // Create buttons
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Create first button
        InlineKeyboardButton randomQuestionBtn = InlineKeyboardButton.builder()
                .text("Сгенерировать рандомный вопрос по java")
                .callbackData(GET_RANDOM_JAVA_QUESTION_BTN) // Set callback data
                .build();
        InlineKeyboardButton randomEngBtn = InlineKeyboardButton.builder()
                .text("Сгенерировать рандомное слово на англ")
                .callbackData(GET_RANDOM_ENGLISH_QUESTION_BTN) // Set callback data
                .build();


        // First row of buttons
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row1.add(randomQuestionBtn);
        row2.add(randomEngBtn);

        buttons.add(row1);
        buttons.add(row2);
        markup.setKeyboard(buttons);

        // Send message with inline keyboard
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите опцию:");
        message.setReplyMarkup(markup);
        setMessageThreadIdIfNeeded(message, String.valueOf(chatId));

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleButtonChatSettings(Long chatId) {
        // Create buttons
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Create first button
        InlineKeyboardButton englishOn = InlineKeyboardButton.builder()
                .text("Включить английский")
                .callbackData(ENGLISH_ON_BTN) // Set callback data
                .build();

        InlineKeyboardButton englishOff = InlineKeyboardButton.builder()
                .text("Выключить английский")
                .callbackData(ENGLISH_OFF_BTN) // Set callback data
                .build();

        // Create second button
        InlineKeyboardButton javaOn = InlineKeyboardButton.builder()
                .text("Включить java")
                .callbackData(JAVA_ON_BTN) // Set callback data
                .build();

        InlineKeyboardButton javaOff = InlineKeyboardButton.builder()
                .text("Выключить java")
                .callbackData(JAVA_OFF_BTN) // Set callback data
                .build();

        // First row of buttons
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(englishOn);
        row1.add(englishOff);

        // Second row of buttons
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(javaOn);
        row2.add(javaOff);

        // Third row of buttons - vacancies
        InlineKeyboardButton vacanciesOn = InlineKeyboardButton.builder()
                .text("Включить вакансии")
                .callbackData(VACANCIES_ON_BTN)
                .build();

        InlineKeyboardButton vacanciesOff = InlineKeyboardButton.builder()
                .text("Выключить вакансии")
                .callbackData(VACANCIES_OFF_BTN)
                .build();

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(vacanciesOn);
        row3.add(vacanciesOff);

        // Fourth row of buttons - vacancy areas management
        InlineKeyboardButton vacanciesSetAreas = InlineKeyboardButton.builder()
                .text("Управление регионами")
                .callbackData(VACANCIES_SET_AREAS_BTN)
                .build();

        InlineKeyboardButton vacanciesShowAreas = InlineKeyboardButton.builder()
                .text("Показать регионы")
                .callbackData(VACANCIES_SHOW_AREAS_BTN)
                .build();

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(vacanciesSetAreas);
        row4.add(vacanciesShowAreas);

        buttons.add(row1);
        buttons.add(row2);
        buttons.add(row3);
        buttons.add(row4);
        markup.setKeyboard(buttons);

        // Send message with inline keyboard
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите опцию:");
        message.setReplyMarkup(markup);
        setMessageThreadIdIfNeeded(message, String.valueOf(chatId));

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Получает message_thread_id для чата (если это форум-группа)
     *
     * @param chatId ID чата
     * @return message_thread_id или null
     */
    private Integer getMessageThreadId(String chatId) {
        ActiveChat activeChat = activeChatService.getActiveChatByChatId(chatId);
        return activeChat != null ? activeChat.getMessageThreadId() : null;
    }

    /**
     * Устанавливает message_thread_id для сообщения, если чат является форум-группой
     *
     * @param message сообщение для отправки
     * @param chatId ID чата
     */
    private void setMessageThreadIdIfNeeded(SendMessage message, String chatId) {
        Integer messageThreadId = getMessageThreadId(chatId);
        if (messageThreadId != null) {
            message.setMessageThreadId(messageThreadId);
        }
    }

    /**
     * Отправляет стандартное сообщение в чат с поддержкой топиков форума
     *
     * @param chatId ID чата
     * @param msg текст сообщения
     */
    public void sendStandardMsg(String chatId, String msg) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(msg);
        setMessageThreadIdIfNeeded(message, chatId);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        
        // Сохраняем message_thread_id если callback из топика форума
        // getMessage() возвращает MaybeInaccessibleMessage, проверяем что это Message
        var maybeMessage = update.getCallbackQuery().getMessage();
        if (maybeMessage instanceof org.telegram.telegrambots.meta.api.objects.Message) {
            org.telegram.telegrambots.meta.api.objects.Message callbackMessage = 
                (org.telegram.telegrambots.meta.api.objects.Message) maybeMessage;
            Integer messageThreadId = callbackMessage.getMessageThreadId();
            if (messageThreadId != null) {
                // Проверяем, что это форум-группа (supergroup с включенными топиками)
                if (callbackMessage.getChat().getIsForum()) {
                    activeChatService.updateMessageThreadId(String.valueOf(chatId), messageThreadId);
                    logger.debug("Сохранен message_thread_id {} для чата {} из callback", messageThreadId, chatId);
                }
            }
        }

        // Respond based on the callback data
        String responseText;
        switch (callbackData) {
            case ENGLISH_ON_BTN:
                activeChatService.updateEnglishByChatId(String.valueOf(chatId), true);
                responseText = "Английский включен";
                break;
            case ENGLISH_OFF_BTN:
                activeChatService.updateEnglishByChatId(String.valueOf(chatId), false);
                responseText = "Английский выключен";
                break;
            case JAVA_ON_BTN:
                activeChatService.updateJavaByChatId(String.valueOf(chatId), true);
                responseText = "Java включена";
                break;
            case JAVA_OFF_BTN:
                responseText = "Java выключена";
                activeChatService.updateJavaByChatId(String.valueOf(chatId), false);
                break;
            case GET_RANDOM_JAVA_QUESTION_BTN:
                JavaMessage javaMessage=javaMessageService.getRandomJavaMessage();
                if(javaMessage!=null){
                    responseText= javaMessage.getQuestionOfTheDay();
                } else {
                    responseText = "Что-то пошло не так. Видимо не сегодня :Р";
                }
                break;
            case GET_RANDOM_ENGLISH_QUESTION_BTN:
                 EnglishMessage englishMessage=englishMessageService.getRandomEnglishMessage();
                if(englishMessage!=null){
                    responseText= englishMessage.getWordOfTheDayMessage();
                } else {
                    responseText = "Что-то пошло не так. Видимо не сегодня :Р";
                }
                break;
            case VACANCIES_ON_BTN:
                handleVacanciesOn(chatId);
                return; // Ответ отправляется внутри метода
            case VACANCIES_OFF_BTN:
                activeChatService.updateVacanciesByChatId(String.valueOf(chatId), false);
                responseText = "❌ Вакансии выключены";
                break;
            case VACANCIES_SET_AREAS_BTN:
                handleVacanciesSetAreas(chatId);
                return; // Ответ отправляется внутри метода
            case VACANCIES_SHOW_AREAS_BTN:
                handleVacancyAreaList(chatId);
                return; // Ответ отправляется внутри метода
            case VACANCIES_REMOVE_AREA_BTN:
                handleVacanciesRemoveArea(chatId);
                return; // Ответ отправляется внутри метода
            default:
                responseText = "Неизвестная кнопка!";
        }

        // Send response
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(responseText);
        setMessageThreadIdIfNeeded(message, String.valueOf(chatId));

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает включение вакансий
     */
    private void handleVacanciesOn(Long chatId) {
        try {
            activeChatService.updateVacanciesByChatId(String.valueOf(chatId), true);
            List<AreaDto> areas = activeChatService.getVacancyAreasWithNames(String.valueOf(chatId));

            StringBuilder message = new StringBuilder();
            message.append("✅ Вакансии включены!\n\n");
            message.append("📋 Текущие регионы для поиска:\n");

            for (AreaDto area : areas) {
                String flag = vacancyMessageFormatter.getFlagByAreaId(area.getId());
                message.append(flag).append(" ").append(area.getName())
                        .append(" (").append(area.getId()).append(")\n");
            }

            message.append("\nИспользуйте команды для управления регионами.");

            sendStandardMsg(String.valueOf(chatId), message.toString());
        } catch (Exception e) {
            logger.error("Ошибка при включении вакансий: {}", e.getMessage(), e);
            sendStandardMsg(String.valueOf(chatId), "❌ Ошибка при включении вакансий");
        }
    }

    /**
     * Обрабатывает меню управления регионами
     */
    private void handleVacanciesSetAreas(Long chatId) {
        try {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

            InlineKeyboardButton showAreas = InlineKeyboardButton.builder()
                    .text("📋 Показать текущие регионы")
                    .callbackData(VACANCIES_SHOW_AREAS_BTN)
                    .build();

            InlineKeyboardButton removeArea = InlineKeyboardButton.builder()
                    .text("➖ Удалить регион")
                    .callbackData(VACANCIES_REMOVE_AREA_BTN)
                    .build();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(showAreas);
            row1.add(removeArea);

            buttons.add(row1);
            markup.setKeyboard(buttons);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Управление регионами для поиска вакансий:\n\n" +
                    "Для добавления региона используйте команду:\n" +
                    "vacancies_area_add <название>");
            message.setReplyMarkup(markup);
            setMessageThreadIdIfNeeded(message, String.valueOf(chatId));

            execute(message);
        } catch (Exception e) {
            logger.error("Ошибка при показе меню управления регионами: {}", e.getMessage(), e);
            sendStandardMsg(String.valueOf(chatId), "❌ Ошибка при открытии меню");
        }
    }

    /**
     * Обрабатывает показ списка регионов
     */
    private void handleVacancyAreaList(Long chatId) {
        try {
            List<AreaDto> areas = activeChatService.getVacancyAreasWithNames(String.valueOf(chatId));

            if (areas.isEmpty()) {
                sendStandardMsg(String.valueOf(chatId),
                        "📋 Список регионов пуст. Используются дефолтные регионы: 🇷🇺 Россия (113), 🇧🇾 Беларусь (16)");
                return;
            }

            StringBuilder message = new StringBuilder();
            message.append("📋 Ваши регионы для поиска вакансий:\n\n");

            for (AreaDto area : areas) {
                String flag = vacancyMessageFormatter.getFlagByAreaId(area.getId());
                message.append(flag).append(" ").append(area.getName())
                        .append(" (").append(area.getId()).append(")\n");
            }

            message.append("\nВсего: ").append(areas.size()).append(" регионов");

            sendStandardMsg(String.valueOf(chatId), message.toString());
        } catch (Exception e) {
            logger.error("Ошибка при показе списка регионов: {}", e.getMessage(), e);
            sendStandardMsg(String.valueOf(chatId), "❌ Ошибка при получении списка регионов");
        }
    }

    /**
     * Обрабатывает удаление региона
     */
    private void handleVacanciesRemoveArea(Long chatId) {
        try {
            List<AreaDto> areas = activeChatService.getVacancyAreasWithNames(String.valueOf(chatId));

            if (areas.isEmpty()) {
                sendStandardMsg(String.valueOf(chatId),
                        "📋 Список регионов пуст. Нечего удалять.");
                return;
            }

            sendStandardMsg(String.valueOf(chatId),
                    "Для удаления региона используйте команду:\n" +
                            "vacancies_area_remove <название>\n\n" +
                            "Текущие регионы:\n" +
                            areas.stream()
                                    .map(area -> vacancyMessageFormatter.getFlagByAreaId(area.getId()) +
                                            " " + area.getName())
                                    .collect(Collectors.joining("\n")));
        } catch (Exception e) {
            logger.error("Ошибка при удалении региона: {}", e.getMessage(), e);
            sendStandardMsg(String.valueOf(chatId), "❌ Ошибка при удалении региона");
        }
    }

    /**
     * Обрабатывает команду добавления региона
     */
    private void handleVacancyAreaAdd(Long chatId, String messageText) {
        try {
            String areaName = messageText.substring(VACANCIES_AREA_ADD_COMMAND.length()).trim();

            if (areaName.isEmpty()) {
                sendStandardMsg(String.valueOf(chatId),
                        "❌ Укажите название региона.\nПример: vacancies_area_add Россия");
                return;
            }

            activeChatService.addVacancyAreaByName(String.valueOf(chatId), areaName);
            handleVacancyAreaList(chatId);

        } catch (AreaNotFoundException e) {
            sendStandardMsg(String.valueOf(chatId), "❌ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            sendStandardMsg(String.valueOf(chatId), "⚠️ " + e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при добавлении региона: {}", e.getMessage(), e);
            sendStandardMsg(String.valueOf(chatId), "❌ Ошибка при добавлении региона");
            sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Обрабатывает команду удаления региона
     */
    private void handleVacancyAreaRemove(Long chatId, String messageText) {
        try {
            String areaName = messageText.substring(VACANCIES_AREA_REMOVE_COMMAND.length()).trim();

            if (areaName.isEmpty()) {
                sendStandardMsg(String.valueOf(chatId),
                        "❌ Укажите название региона.\nПример: vacancies_area_remove Россия");
                return;
            }

            activeChatService.removeVacancyAreaByName(String.valueOf(chatId), areaName);
            sendStandardMsg(String.valueOf(chatId), "✅ Регион '" + areaName + "' удален из списка");
            handleVacancyAreaList(chatId);

        } catch (AreaNotFoundException e) {
            sendStandardMsg(String.valueOf(chatId), "❌ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            sendStandardMsg(String.valueOf(chatId), "⚠️ " + e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при удалении региона: {}", e.getMessage(), e);
            sendStandardMsg(String.valueOf(chatId), "❌ Ошибка при удалении региона");
            sendMeService.sendMe(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }
    }

}
