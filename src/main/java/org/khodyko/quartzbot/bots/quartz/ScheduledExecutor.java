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
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Service
public class ScheduledExecutor {

    private final static String ENG_WORD_OF_THE_DAY_TEMPLATE = """
            Word of the day 
            %s - %s
            Write sentence with this word.
            """;

    private final static String JAVA_QUESTION_TEMPLATE = """
            Вопрос дня: 
            %s
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


    @Scheduled(cron = "0 0 7 * * ?") //7 утра
    public void sendEnglishMessages() {

        List<ActiveChat> engChats = activeChatService.getActiveEnglishChats();
        EnglishMessage randomEnglishMessage = englishMessageService.getRandomEnglishMessage();
        for (ActiveChat chat : engChats) {
            String chatId = chat.getChatId();
            String messageText = String.format(ENG_WORD_OF_THE_DAY_TEMPLATE, randomEnglishMessage.getText(),
                    randomEnglishMessage.getTranslation());
            sendMessageToChatAndPin(messageText, chatId);
        }
    }

    private void sendMessageToChatAndPin(String text, String chatId) {
        if (chatId != null) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            try {
                Message sendedMsg = quartzMessageBot.execute(message);
                Long msgId = sendedMsg.getMessageId().longValue();
                PinChatMessage pinChatMessage = new PinChatMessage(chatId.toString(), Math.toIntExact(msgId));
                quartzMessageBot.execute(pinChatMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(cron = "0 0 7 * * ?") //7 утра
    public void sendJavaMessages() {

        List<ActiveChat> javaChats = activeChatService.getActiveJavaChats();
        JavaMessage javaMessage = null;
        for (ActiveChat chat : javaChats) {
            if (chat.getJavaTopicEnum() != null) {
                javaMessage = javaMessageService.getRandomJavaMessageWithTopic(chat.getJavaTopicEnum());
            } else {
                javaMessage = javaMessageService.getRandomJavaMessage();
            }
            String chatId = chat.getChatId();
            if (javaMessage != null) {
                String javaText = javaMessage.getText();
                sendMessageToChatAndPin(javaText, chatId);
            } else {
                sendMessageToChatAndPin("Что-то пошло не так. Может в следующий раз :р", chatId);
            }

        }
    }


}
