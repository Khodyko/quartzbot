package org.khodyko.quartzbot.bots.quartz;

import org.khodyko.quartzbot.config.BotConfig;
import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.model.JavaMessage;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.khodyko.quartzbot.service.JavaMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuartzMessageBot extends TelegramLongPollingBot {

    private static final String ENGLISH_ON_BTN = "englishOn";
    private static final String ENGLISH_OFF_BTN = "englishOff";
    private static final String JAVA_ON_BTN = "javaOn";
    private static final String JAVA_OFF_BTN = "javaOff";

    private static final String GET_RANDOM_JAVA_QUESTION_BTN= "getRandomJavaQuestionBtn";
    private static final String JAVA_TOPIC_SET_COMMAND = "java_topic_set_bot";
    private static final String JAVA_TOPIC_GET_ALL_COMMAND = "java_topic_get_all";
    Logger logger = LoggerFactory.getLogger(QuartzMessageBot.class);

    private final BotConfig botConfig;
    private ActiveChatService activeChatService;
    private JavaMessageService javaMessageService;

    @Autowired
    public QuartzMessageBot( BotConfig botConfig, ActiveChatService activeChatService, JavaMessageService javaMessageService) {
        this.botConfig = botConfig;
        this.activeChatService = activeChatService;
        this.javaMessageService = javaMessageService;
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
        Long chatId = update.getMessage().getChatId();
        String msgTxt = update.getMessage().getText();
        if (msgTxt != null) {
            if(msgTxt.equals("@" + botConfig.getBotName())){
                handleButtonGetQuestion(chatId);
            } else if (msgTxt.equals("@" + botConfig.getBotName()+"_chat_settings")) {
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
            } else if (msgTxt.equals(JAVA_TOPIC_GET_ALL_COMMAND)){
                sendStandardMsg(String.valueOf(chatId), JavaTopicEnum.toStringTopicNames());
            }
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



        // First row of buttons
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(randomQuestionBtn);

        buttons.add(row1);
        markup.setKeyboard(buttons);

        // Send message with inline keyboard
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите опцию:");
        message.setReplyMarkup(markup);

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

        buttons.add(row1);
        buttons.add(row2);
        markup.setKeyboard(buttons);

        // Send message with inline keyboard
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите опцию:");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStandardMsg(String chatId, String msg) {
        // Send message with inline keyboard
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(msg);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

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
                    responseText= String.format("Внимание вопрос! \n %s", javaMessage.getText());
                } else {
                    responseText = "Что-то пошло не так. Видимо не сегодня :Р";
                }
                break;
            default:
                responseText = "Неизвестная кнопка!";
        }

        // Send response
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
