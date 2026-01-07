package org.khodyko.quartzbot.repository;

import org.khodyko.quartzbot.model.ActiveChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveChatRepository extends JpaRepository<ActiveChat, Long> {

    List<ActiveChat> findByEnglish(boolean english);

    List<ActiveChat> findByJava(boolean java);

    // New method to find by chatId
    ActiveChat findByChatId(String chatId);

    List<ActiveChat> findByVacancies(boolean vacancies);
}
