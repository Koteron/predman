package com.predman.content.service;

import com.predman.content.dto.grpc.PredictionDto;
import com.predman.content.dto.project.*;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectMember;
import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.ProjectMapper;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectMemberService projectMemberService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ProjectStatisticsService projectStatisticsService;
    private final TaskService taskService;
    private final StatisticsService statisticsService;
    private final UserMapper userMapper;

    ProjectServiceImpl(@Lazy ProjectMemberService projectMemberService,
                       @Lazy UserService userService,
                       ProjectRepository projectRepository,
                       ProjectMapper projectMapper,
                       EntityManager entityManager,
                       @Lazy ProjectStatisticsService projectStatisticsService,
                       @Lazy TaskService taskService,
                       StatisticsService statisticsService, UserMapper userMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.projectMemberService = projectMemberService;
        this.entityManager = entityManager;
        this.userService = userService;
        this.projectStatisticsService = projectStatisticsService;
        this.statisticsService = statisticsService;
        this.taskService = taskService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public ProjectDto create(ProjectCreationDto projectCreationDto) {
        Project project = projectRepository.save(Project
                .builder()
                .name(projectCreationDto.name())
                .description(projectCreationDto.description())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .dueDate(projectCreationDto.dueDate())
                .certaintyPercent(0.0)
                .availableHours(0.0)
                .sumExperience(0.0)
                .externalRiskProbability(0.0)
                .predictedDeadline(LocalDate.now())
                .owner(userService.getAuthenticatedUser())
                .build());
        projectMemberService.addProjectMember(userService.getAuthenticatedUser(), project);
        projectStatisticsService.initializeStatistics(project);
        return projectMapper.convertToProjectDto(project);
    }

    @Override
    public ProjectFullInfoDto update(Project project, ProjectUpdateDto projectUpdateDto) {
        if (projectUpdateDto.name() != null && projectUpdateDto.name().isEmpty()) {
            throw new ForbiddenException("Project name cannot be empty");
        }
        Project updatedProject = Project.builder()
                .id(project.getId())
                .description(projectUpdateDto.description() == null ? project.getDescription()
                        : projectUpdateDto.description())
                .name(projectUpdateDto.name() == null ? project.getName()
                        : projectUpdateDto.name())
                .dueDate(projectUpdateDto.dueDate() == null ? project.getDueDate()
                        : projectUpdateDto.dueDate())
                .availableHours(projectUpdateDto.availableHours() == null ? project.getAvailableHours()
                        : projectUpdateDto.availableHours())
                .sumExperience(projectUpdateDto.sumExperience() == null ? project.getSumExperience()
                        : projectUpdateDto.sumExperience())
                .externalRiskProbability(projectUpdateDto.externalRiskProbability() == null ?
                        project.getExternalRiskProbability()
                        : projectUpdateDto.externalRiskProbability())
                .owner(project.getOwner())
                .updatedDate(LocalDateTime.now())
                .createdDate(project.getCreatedDate())
                .build();
        if (projectUpdateDto.dueDate() != null || projectUpdateDto.availableHours() != null
                || projectUpdateDto.externalRiskProbability() != null
                || projectUpdateDto.sumExperience() != null)
        {
            projectStatisticsService.updateStatisticsByUpdatedProject(updatedProject);
            PredictionDto prediction = statisticsService.getPrediction(updatedProject.getId(),
                    (int) ChronoUnit.DAYS.between(
                    updatedProject.getCreatedDate().toLocalDate(),
                    projectUpdateDto.dueDate() == null ? updatedProject.getDueDate()
                            : projectUpdateDto.dueDate()));
            updatedProject.setCertaintyPercent(prediction.certaintyPercent());
            updatedProject.setPredictedDeadline(
                    project.getCreatedDate().toLocalDate().plusDays(prediction.predictedDays()));
        }
        else {
            updatedProject.setPredictedDeadline(project.getPredictedDeadline());
            updatedProject.setCertaintyPercent(project.getCertaintyPercent());
        }
        return projectMapper.convertToProjectFullInfoDto(projectRepository.save(updatedProject));
    }

    @Override
    public ProjectFullInfoDto updatePrediction(Project project) {
        PredictionDto prediction;
        try {
            prediction = statisticsService.getPrediction(project.getId(), (int) ChronoUnit.DAYS.between(
                    project.getCreatedDate().toLocalDate(), project.getDueDate()));
        }
        catch (Exception e) {
            prediction = PredictionDto.builder().predictedDays(0).certaintyPercent(0.0).build();
        }
        return projectMapper.convertToProjectFullInfoDto(projectRepository.save(Project.builder()
                .id(project.getId())
                .description(project.getDescription())
                .name(project.getName())
                .dueDate(project.getDueDate())
                .owner(project.getOwner())
                .sumExperience(project.getSumExperience())
                .availableHours(project.getAvailableHours())
                .externalRiskProbability(project.getExternalRiskProbability())
                .predictedDeadline(project.getCreatedDate().toLocalDate().plusDays(prediction.predictedDays()))
                .certaintyPercent(prediction.certaintyPercent())
                .updatedDate(LocalDateTime.now())
                .createdDate(project.getCreatedDate())
                .build()));
    }

    @Override
    public ProjectFullInfoDto getFullInfoById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project owner not found!"));
        return updatePrediction(project);
    }

    @Override
    public ProjectDto getById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project owner not found!"));
        return projectMapper.convertToProjectDto(project);
    }

    @Override
    public ProjectTaskListDto getWithTaskListById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project owner not found!"));
        return ProjectTaskListDto
                .builder()
                .id(project.getId())
                .ownerId(project.getOwner().getId())
                .name(project.getName())
                .description(project.getDescription())
                .tasks(taskService.getAllByProjectId(project.getId()))
                .build();
    }

    @Override
    public Project getEntityById(UUID id) {
        Project project = entityManager.find(Project.class, id);
        if (project == null) {
            throw new NotFoundException("Project not found!");
        }
        return project;
    }

    @Override
    public List<ProjectDto> getAllByOwnerId(UUID ownerId) {
        return projectRepository.findByOwner_Id(ownerId)
                .stream().map(projectMapper::convertToProjectDto).toList();
    }

    @Override
    public List<Project> getAllEntitiesByOwnerId(UUID ownerId) {
        return projectRepository.findByOwner_Id(ownerId);
    }

    @Override
    public void delete(Project project) {
        projectRepository.deleteById(project.getId());
    }

    @Override
    public ProjectDto changeOwnerUnchecked(Project project, User newOwner) {
        project.setOwner(newOwner);
        return projectMapper.convertToProjectDto(projectRepository.save(project));
    }

    @Override
    public ProjectDto changeOwnerChecked(Project project, String newOwnerEmail) {
        User user = userService.getEntityByEmail(newOwnerEmail);
        if (projectMemberService.getUsersByProjectId(project.getId()).stream().noneMatch((UserDto userDto) ->
                userDto.id() == user.getId()))
        {
            throw new NotFoundException("User is not a part of this project!");
        }
        return changeOwnerUnchecked(project, userService.getEntityByEmail(newOwnerEmail));
    }

    @Override
    public void changeOwnerRandom(UUID previousOwnerId, UUID projectId) {
        List<UserDto> members = projectMemberService.getUsersByProjectId(projectId);
        List<UserDto> alternativeOwners = members.stream()
                .filter(u -> !u.id().equals(previousOwnerId))
                .toList();

        if (!alternativeOwners.isEmpty()) {
            User newOwner = userMapper.convertToUserEntity(
                    alternativeOwners.get(ThreadLocalRandom.current().nextInt(alternativeOwners.size())));
            changeOwnerUnchecked(getEntityById(projectId), newOwner);
        } else {
            projectRepository.deleteById(projectId);
        }
    }
}
