package org.khodyko.quartzbot.service.impl;

import org.khodyko.quartzbot.bots.quartz.QuartzMessageBot;
import org.khodyko.quartzbot.service.SendMeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class SendMe implements SendMeService {
    @Value("${bot.igor.chat.id}")
    private String myChatId;
    private QuartzMessageBot quartzMessageBot;

    @Autowired
    public SendMe(QuartzMessageBot quartzMessageBot) {
        this.quartzMessageBot = quartzMessageBot;
    }

    @Override
    public void sendMe(List<String> messages){
        SendMessage sendMessage;
        for(String message:messages){
           sendMessage=new SendMessage();
            sendMessage.setChatId(myChatId);

            sendMessage.setText(message);
            try {
                quartzMessageBot.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    };

}
