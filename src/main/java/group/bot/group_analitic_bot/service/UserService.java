package group.bot.group_analitic_bot.service;

import group.bot.group_analitic_bot.entity.User;
import group.bot.group_analitic_bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserFindChatId(Long userId) {
        return userRepository.findByChatId(userId).orElse(null);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
