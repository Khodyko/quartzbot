package org.khodyko.quartzbot.model;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractMessage {


    private String text;

    public AbstractMessage( String text) {

        this.text = text;
    }

    public AbstractMessage() {
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
