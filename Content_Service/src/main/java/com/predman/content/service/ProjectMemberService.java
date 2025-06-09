package com.predman.content.service;

import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.project_member.ProjectMemberDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectMember;
import com.predman.content.entity.User;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberService {
    List<UserDto> getUsersByProjectId(UUID projectId);
    List<ProjectDto> getProjectsByUserId(UUID userId);
    UserDto addProjectMember(User fetchedUser, Project fetchedProject);
    UserDto addProjectMemberStatUpdate(User fetchedUser, Project fetchedProject);
    void removeProjectMember(ProjectMemberDto projectMemberDto);
    List<ProjectMember> findAllByProjectIds(List<UUID> projectIds);
    void deleteAllByProjectId (UUID projectId);
    void deleteAllByUserId (UUID userId);

}
