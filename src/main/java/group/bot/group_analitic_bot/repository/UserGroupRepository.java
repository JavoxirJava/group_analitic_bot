package group.bot.group_analitic_bot.repository;

import group.bot.group_analitic_bot.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    Optional<UserGroup> findByUserIdAndGroupId(Long userChatId, Long groupChatId);

    List<UserGroup> findByGroupId(Long groupChatId); // SELECT * FROM user_group WHERE group_chat_id = ?1>

    List<UserGroup> findByUserId(Long userChatId); // SELECT * FROM user_group WHERE user_chat_id = ?1>
}
