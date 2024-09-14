package org.khodyko.quartzbot.bots.quartz;

import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.model.EnglishMessage;
import org.khodyko.quartzbot.model.JavaMessage;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.khodyko.quartzbot.service.EnglishMessageService;
import org.khodyko.quartzbot.service.JavaMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Service
public class ScheduledExecutor {

    private final static String WORD_OF_THE_DAY_TEMPLATE= """
            Word of the day %s - %s
            Write sentence with this word.
            """;

    private ActiveChatService activeChatService;
    private QuartzMessageBot quartzMessageBot;

    private EnglishMessageService englishMessageService;

    private JavaMessageService javaMessageService;

    @Autowired
    public ScheduledExecutor(ActiveChatService activeChatService, QuartzMessageBot quartzMessageBot, EnglishMessageService englishMessageService, JavaMessageService javaMessageService) {
        this.activeChatService = activeChatService;
        this.quartzMessageBot = quartzMessageBot;
        this.englishMessageService = englishMessageService;
        this.javaMessageService = javaMessageService;
    }

    // Метод, который будет вызываться с заданной периодичностью
    @Scheduled(fixedRate = 30000) // Отправка сообщения каждые 60 секунд
    public void sendEnglishMessages() {

        List<ActiveChat> engChats = activeChatService.getActiveEnglishChats();
        EnglishMessage randomEnglishMessage = englishMessageService.getRandomEnglishMessage();
        for (ActiveChat chat : engChats) {
            String chatId = chat.getChatId();
            String messageText =String.format(WORD_OF_THE_DAY_TEMPLATE, randomEnglishMessage.getText(),
                    randomEnglishMessage.getTranslation());
            sendMessageToChat(messageText, chatId);
        }
    }

    private void sendMessageToChat(String text, String chatId) {
        if (chatId != null) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            try {
                quartzMessageBot.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Метод, который будет вызываться с заданной периодичностью
    @Scheduled(fixedRate = 30000) // Отправка сообщения каждые 60 секунд
    public void sendJavaMessages() {

        List<ActiveChat> javaChats = activeChatService.getActiveJavaChats();
        JavaMessage javaMessage = javaMessageService.getRandomJavaMessage();

        for (ActiveChat chat : javaChats) {
            if(chat.getJavaTopicEnum()!=null){
                javaMessage=javaMessageService.getRandomJavaMessageWithTopic(chat.getJavaTopicEnum());
            } else {
                javaMessage=javaMessageService.getRandomJavaMessage();
            }
            String chatId = chat.getChatId();
            String javaText = javaMessage.getText();
            sendMessageToChat(javaText, chatId);
        }
    }


}
