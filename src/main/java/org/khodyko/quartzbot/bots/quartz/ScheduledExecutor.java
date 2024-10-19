package org.khodyko.quartzbot.bots.quartz;

import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.model.EnglishMessage;
import org.khodyko.quartzbot.model.JavaMessage;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.khodyko.quartzbot.service.EnglishMessageService;
import org.khodyko.quartzbot.service.JavaMessageService;
import org.khodyko.quartzbot.service.SendMeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledExecutor {
    private ActiveChatService activeChatService;
    private QuartzMessageBot quartzMessageBot;

    private EnglishMessageService englishMessageService;

    private JavaMessageService javaMessageService;

    private SendMeService sendMeService;

    @Autowired
    public ScheduledExecutor(ActiveChatService activeChatService, QuartzMessageBot quartzMessageBot, EnglishMessageService englishMessageService, JavaMessageService javaMessageService, SendMeService sendMeService) {
        this.activeChatService = activeChatService;
        this.quartzMessageBot = quartzMessageBot;
        this.englishMessageService = englishMessageService;
        this.javaMessageService = javaMessageService;
        this.sendMeService = sendMeService;
    }

    @Scheduled(cron = "0 0 7 * * ?") //7 утра
    public void sendEnglishMessages() {
        try {
            List<ActiveChat> engChats = activeChatService.getActiveEnglishChats();
            EnglishMessage randomEnglishMessage = englishMessageService.getRandomEnglishMessage();
            for (ActiveChat chat : engChats) {
                String chatId = chat.getChatId();
                String messageText = randomEnglishMessage.getWordOfTheDayMessage();
                sendMessageToChatAndPin(messageText, chatId);
            }
        } catch (Exception e) {
            sendMeService.sendMe(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()));
        }
    }

    private void sendMessageToChatAndPin(String text, String chatId) {
        try {
            if (chatId != null) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(text);

                Message sendedMsg = quartzMessageBot.execute(message);
                Long msgId = sendedMsg.getMessageId().longValue();
                PinChatMessage pinChatMessage = new PinChatMessage(chatId.toString(), Math.toIntExact(msgId));
                quartzMessageBot.execute(pinChatMessage);

            }
        } catch (Exception e) {
            sendMeService.sendMe(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()));
        }
    }

    @Scheduled(cron = "0 0 7 * * ?") //7 утра
    public void sendJavaMessages() {
        try {
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
                    String javaText = javaMessage.getQuestionOfTheDay();
                    sendMessageToChatAndPin(javaText, chatId);
                } else {
                    sendMessageToChatAndPin("Что-то пошло не так. Может в следующий раз :р", chatId);
                }
            }
        } catch (Exception e) {
            sendMeService.sendMe(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()));
        }
    }


}
