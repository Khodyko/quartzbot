package org.khodyko.quartzbot.bots.quartz;

import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.model.EnglishMessage;
import org.khodyko.quartzbot.model.JavaMessage;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.khodyko.quartzbot.service.EnglishMessageService;
import org.khodyko.quartzbot.service.JavaMessageService;
import org.khodyko.quartzbot.service.SendMeService;
import org.khodyko.quartzbot.service.VacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledExecutor.class);

    private ActiveChatService activeChatService;
    private QuartzMessageBot quartzMessageBot;
    private EnglishMessageService englishMessageService;
    private JavaMessageService javaMessageService;
    private SendMeService sendMeService;
    private VacancyService vacancyService;

    @Autowired
    public ScheduledExecutor(ActiveChatService activeChatService, QuartzMessageBot quartzMessageBot, EnglishMessageService englishMessageService, JavaMessageService javaMessageService, SendMeService sendMeService, VacancyService vacancyService) {
        this.activeChatService = activeChatService;
        this.quartzMessageBot = quartzMessageBot;
        this.englishMessageService = englishMessageService;
        this.javaMessageService = javaMessageService;
        this.sendMeService = sendMeService;
        this.vacancyService = vacancyService;
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

                // Получаем ActiveChat для проверки наличия message_thread_id (для форум-групп)
                ActiveChat activeChat = activeChatService.getActiveChatByChatId(chatId);
                if (activeChat != null && activeChat.getMessageThreadId() != null) {
                    // Если есть сохраненный thread_id, используем его (это форум-группа)
                    message.setMessageThreadId(activeChat.getMessageThreadId());
                }

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

//    @Scheduled(cron = "0 */2 * * * ?") // Каждые 2 минуты
    @Scheduled(cron = "0 0 8 * * ?") // 8:00 утра
    public void sendVacancyMessages() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
        vacancyService.searchAndSendVacancies(dateStr);
    }

}
