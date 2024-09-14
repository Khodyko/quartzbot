package org.khodyko.quartzbot.repository;

import org.khodyko.quartzbot.model.EnglishMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnglishMessageRepository extends JpaRepository<EnglishMessage, Long> {

    @Query(value = "SELECT * FROM english_message ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    public EnglishMessage getRandomEnglishMessage();

}
