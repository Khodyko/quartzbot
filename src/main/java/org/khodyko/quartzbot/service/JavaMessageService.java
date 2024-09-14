package org.khodyko.quartzbot.service;

import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.model.JavaMessage;

public interface JavaMessageService {
    JavaMessage getRandomJavaMessage();

    JavaMessage getRandomJavaMessageWithTopic(JavaTopicEnum javaTopicEnum);
}
