package com.predman.content.mapper;

import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.project.ProjectFullInfoDto;
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

    public Project convertToProjectEntity(final ProjectFullInfoDto projectFullInfoDto) {
        return Project
                .builder()
                .id(projectFullInfoDto.id())
                .name(projectFullInfoDto.name())
                .description(projectFullInfoDto.description())
                .dueDate(projectFullInfoDto.dueDate())
                .owner(userMapper.convertToUserEntity(userService.getById(projectFullInfoDto.ownerId())))
                .externalRiskProbability(projectFullInfoDto.externalRiskProbability())
                .sumExperience(projectFullInfoDto.sumExperience())
                .availableHours(projectFullInfoDto.availableHours())
                .build();
    }

    public ProjectFullInfoDto convertToProjectFullInfoDto(final Project project) {
        return ProjectFullInfoDto
                .builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .externalRiskProbability(project.getExternalRiskProbability())
                .sumExperience(project.getSumExperience())
                .availableHours(project.getAvailableHours())
                .dueDate(project.getDueDate())
                .certaintyPercent(project.getCertaintyPercent())
                .predictedDeadline(project.getPredictedDeadline())
                .ownerId(project.getOwner().getId())
                .build();
    }

    public ProjectDto convertToProjectDto(final Project project) {
        return ProjectDto
                .builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .build();
    }
}
