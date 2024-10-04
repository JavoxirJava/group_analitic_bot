package group.bot.group_analitic_bot.repository;

import group.bot.group_analitic_bot.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByChatId(Long chatId);

    @Query(value = "DELETE FROM groups WHERE chat_id = ?1", nativeQuery = true)
    void deleteGroupByChatId(Long chatId);
}
