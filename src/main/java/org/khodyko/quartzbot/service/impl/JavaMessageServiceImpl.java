package org.khodyko.quartzbot.service.impl;

import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.model.JavaMessage;
import org.khodyko.quartzbot.repository.JavaMessageRepository;
import org.khodyko.quartzbot.service.JavaMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JavaMessageServiceImpl implements JavaMessageService {

    private JavaMessageRepository javaMessageRepository;

    @Autowired
    public JavaMessageServiceImpl(JavaMessageRepository javaMessageRepository) {
        this.javaMessageRepository = javaMessageRepository;
    }

    @Override
    public JavaMessage getRandomJavaMessage(){
        return javaMessageRepository.getRandomJavaMessage();
    }

    @Override
    public JavaMessage getRandomJavaMessageWithTopic(JavaTopicEnum javaTopicEnum) {
        return javaMessageRepository.getRandomJavaMessageWithTopic(javaTopicEnum.name());
    }
}
