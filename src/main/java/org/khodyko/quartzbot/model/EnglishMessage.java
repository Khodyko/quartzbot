package org.khodyko.quartzbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "english_message")
public class EnglishMessage extends AbstractMessage {
    private final static String ENG_WORD_OF_THE_DAY_TEMPLATE = """
            Word of the day 
            %s [%s] - %s
            Write sentence with this word.
            """;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    private String transcription;

    private String translation;

    @Transient
    public String getWordOfTheDayMessage() {
        return String.format(ENG_WORD_OF_THE_DAY_TEMPLATE, getText(),
                getTranscription() == null ? "" : getTranscription(), getTranslation());
    }

}
