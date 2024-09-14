package org.khodyko.quartzbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.khodyko.quartzbot.enums.JavaTopicEnum;

@Getter
@Setter
@Entity
@Table(name = "active_chats")
public class ActiveChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    // Getters and Setters

    @Column(unique = true)
    private String chatId; // Unique chat identifier

    private boolean english; // Indicates if the chat is in English
    private boolean java;    // Indicates if the chat is related to Java

    @Enumerated(EnumType.STRING)
    private JavaTopicEnum javaTopicEnum;
}
