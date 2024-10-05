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

    public User getUserFindId(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User saveUser(User user) {
        User user2 = getUserFindId(user.getId());
        if (user2 != null) return user2;
        return userRepository.save(user);
    }
}
