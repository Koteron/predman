package com.predman.content.mapper;

import com.predman.content.dto.project.ProjectDto;
import com.predman.content.entity.Project;
import com.predman.content.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    private final UserService userService;
    private final UserMapper userMapper;

    public ProjectMapper(@Lazy UserService userService,
                         UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    public Project convertToProjectEntity(final ProjectDto projectDto) {
        return Project
                .builder()
                .id(projectDto.id())
                .name(projectDto.name())
                .description(projectDto.description())
                .dueDate(projectDto.dueDate())
                .owner(userMapper.convertToUserEntity(userService.getById(projectDto.ownerId())))
                .build();
    }

    public ProjectDto convertToProjectDto(final Project project) {
        return ProjectDto
                .builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .dueDate(project.getDueDate())
                .ownerId(project.getOwner().getId())
                .build();
    }
}
