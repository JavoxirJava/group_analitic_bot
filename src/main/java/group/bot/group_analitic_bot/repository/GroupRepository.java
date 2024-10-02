package group.bot.group_analitic_bot.repository;

import group.bot.group_analitic_bot.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByChatId(Long chatId);

    void deleteByChatId(Long chatId);
}
