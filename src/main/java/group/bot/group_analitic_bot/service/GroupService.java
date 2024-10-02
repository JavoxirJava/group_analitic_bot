package group.bot.group_analitic_bot.service;

import group.bot.group_analitic_bot.entity.Group;
import group.bot.group_analitic_bot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupChatById(Long id) {
        return groupRepository.findByChatId(id).orElse(null);
    }

    public void addGroup(Group group) {
        groupRepository.save(group);
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteByChatId(id);
    }
}
