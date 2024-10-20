package org.khodyko.quartzbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.khodyko.quartzbot.enums.JavaTopicEnum;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table
public class JavaMessage extends AbstractMessage {

    private final static String JAVA_QUESTION_TEMPLATE = """
            Вопрос дня: 
            %s
            """;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Enumerated(EnumType.STRING)
    private JavaTopicEnum topic;

    public String getQuestionOfTheDay(){
        return String.format(JAVA_QUESTION_TEMPLATE, getText());
    }
}
