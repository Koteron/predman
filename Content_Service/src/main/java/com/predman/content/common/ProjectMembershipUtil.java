package com.predman.content.common;

import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.service.ProjectMemberService;
import com.predman.content.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectMembershipUtil {
    private final UserService userService;
    private final ProjectMemberService projectMemberService;

    public void checkSelfProjectMembership(UUID projectId)
    {
        User user = userService.getAuthenticatedUser();
        checkProjectMembership(user.getId(), projectId);
    }

    public void checkProjectMembership(UUID userId, UUID projectId)
    {
        if (projectMemberService.getUsersByProjectId(projectId).stream().noneMatch(userDto ->
                userDto.id().equals(userId))) {
            throw new ForbiddenException("User is not part of this project");
        }
    }
}
