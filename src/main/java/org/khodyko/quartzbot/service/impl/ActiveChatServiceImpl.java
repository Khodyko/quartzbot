package org.khodyko.quartzbot.service.impl;

import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.model.ActiveChat;
import org.khodyko.quartzbot.repository.ActiveChatRepository;
import org.khodyko.quartzbot.service.ActiveChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActiveChatServiceImpl implements ActiveChatService {
    @Autowired
    private ActiveChatRepository activeChatRepository;

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
}
