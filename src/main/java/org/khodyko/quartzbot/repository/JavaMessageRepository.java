package org.khodyko.quartzbot.repository;

import org.khodyko.quartzbot.enums.JavaTopicEnum;
import org.khodyko.quartzbot.model.JavaMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaMessageRepository extends JpaRepository<JavaMessage, Long> {

    @Query(value = "SELECT * FROM java_message ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    JavaMessage getRandomJavaMessage();

    @Query(value = "SELECT * FROM java_message WHERE topic = :topic ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    JavaMessage getRandomJavaMessageWithTopic(@Param("topic") String javaTopicEnumName);
}
