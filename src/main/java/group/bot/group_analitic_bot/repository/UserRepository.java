package group.bot.group_analitic_bot.repository;

import group.bot.group_analitic_bot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
