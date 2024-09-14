package org.khodyko.quartzbot.service;

import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.model.ActiveChat;

import java.util.List;

public interface ActiveChatService {
    ActiveChat updateEnglishByChatId(String chatId, boolean isEnglish);

    ActiveChat updateJavaByChatId(String chatId, boolean isJava);

    List<ActiveChat> getActiveJavaChats();

    List<ActiveChat> getActiveEnglishChats();

    ActiveChat setActiveChatTopicByString(String chatId, JavaTopicEnum javaTopicEnum);
}
