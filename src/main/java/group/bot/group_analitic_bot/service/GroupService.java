package group.bot.group_analitic_bot.service;

import group.bot.group_analitic_bot.entity.Group;
import group.bot.group_analitic_bot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

    public void addGroup(Group group) {
        groupRepository.save(group);
    }

    public void editGroupAddCountByChatId(Long chatId, Integer addCount) {
        Optional<Group> group = groupRepository.findById(chatId);
        if (group.isPresent()) {
            group.get().setAddCount(addCount);
            groupRepository.save(group.get());
        }
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }
}
