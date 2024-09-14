package org.khodyko.quartzbot.service.impl;

import org.khodyko.quartzbot.model.EnglishMessage;
import org.khodyko.quartzbot.repository.EnglishMessageRepository;
import org.khodyko.quartzbot.service.EnglishMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnglishMessageServiceImpl implements EnglishMessageService {

    private EnglishMessageRepository repository;

    @Autowired
    public EnglishMessageServiceImpl(EnglishMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public EnglishMessage getRandomEnglishMessage(){
        return repository.getRandomEnglishMessage();
    }
}
