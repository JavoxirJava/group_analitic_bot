package group.bot.group_analitic_bot.service;

import group.bot.group_analitic_bot.entity.UserGroup;
import group.bot.group_analitic_bot.repository.UserGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserGroupService {

    final UserGroupRepository userGroupRepository;

    public UserGroupService(UserGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }

    public List<UserGroup> getUserGroupByGroup(Long groupId) {
        return userGroupRepository.findByGroupId(groupId);
    }

    public List<UserGroup> getUserGroupByUser(Long userId) {
        return userGroupRepository.findByUserId(userId);
    }

    public UserGroup getUserGroupByUserAndGroup(Long userId, Long groupId) {
        return userGroupRepository.findByUserIdAndGroupId(userId, groupId).orElse(null);
    }

    public void saveUserGroup(UserGroup userGroup) {
        if (userGroupRepository.findByUserIdAndGroupId(userGroup.getUser().getId(), userGroup.getGroup().getId()).isEmpty())
            userGroupRepository.save(userGroup);
    }

    public void editUserGroupActive(Long userId, Long groupId, Boolean isActive) {
        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId).orElse(null);
        assert userGroup != null;
        userGroup.setIsActive(isActive);
        userGroupRepository.save(userGroup);
    }
}
