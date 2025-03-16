package com.predman.content.service;

import com.predman.content.common.TaskStatus;
import com.predman.content.dto.project_statistics.ProjectStatisticsDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectStatistics;
import com.predman.content.repository.ProjectStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectStatisticsServiceImpl implements ProjectStatisticsService {
    private final ProjectStatisticsRepository projectStatisticsRepository;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final TaskService taskService;

    @Override
    public List<ProjectStatisticsDto> getProjectStatisticsByProjectId(UUID projectId) {
        updateStatistics(projectId);
        List<ProjectStatistics> projectStatisticsList = projectStatisticsRepository.findAllByProjectId(projectId);
        return projectStatisticsList.stream().map(projectStatistics ->
                ProjectStatisticsDto
                        .builder()
                        .id(projectStatistics.getId())
                        .projectId(projectId)
                        .completedEasyTasks(projectStatistics.getCompletedEasyTasks())
                        .completedMediumTasks(projectStatistics.getCompletedMediumTasks())
                        .completedHardTasks(projectStatistics.getCompletedHardTasks())
                        .completionProbability(projectStatistics.getCompletionProbability())
                        .daysSinceStart(projectStatistics.getDaysSinceStart())
                        .daysToDeadline(projectStatistics.getDaysToDeadline())
                        .easyTasks(projectStatistics.getEasyTasks())
                        .mediumTasks(projectStatistics.getMediumTasks())
                        .hardTasks(projectStatistics.getHardTasks())
                        .estimatedDate(projectStatistics.getEstimatedDate())
                        .teamSize(projectStatistics.getTeamSize())
                        .savedAt(projectStatistics.getSavedAt())
                        .build()).toList();
    }

    @Override
    public ProjectStatistics updateStatistics(UUID projectId) {
        ProjectStatistics projectStatistics = projectStatisticsRepository.findLatestStatisticsByProjectId(projectId);
        ProjectStatistics updatedProjectStatistics = reevaluateProjectStatistics(projectId);
        if (ChronoUnit.HOURS.between(projectStatistics.getSavedAt(), LocalDateTime.now()) <= 24) {
            updatedProjectStatistics.setId(projectStatistics.getId());
        }
        return projectStatisticsRepository.save(updatedProjectStatistics);
    }

    @Override
    public void initializeStatistics(Project project) {
        ProjectStatistics projectStatistics = ProjectStatistics
                .builder()
                .project(project)
                .completedEasyTasks(0)
                .completedMediumTasks(0)
                .completedHardTasks(0)
                .easyTasks(0)
                .mediumTasks(0)
                .hardTasks(0)
                .teamSize(1)
                .daysToDeadline((int) ChronoUnit.DAYS.between(LocalDate.now(), project.getDueDate()))
                .daysSinceStart(0)
                .savedAt(LocalDateTime.now())
                // Completion probability - Request to NN service
                // Estimated date - Request to NN service
                .build();
        projectStatisticsRepository.save(projectStatistics);
    }

    @Override
    public List<ProjectStatistics> getAllLatestStatistics() {
        return projectStatisticsRepository.findLatestStatisticsForAllProjects();
    }

    private ProjectStatistics reevaluateProjectStatistics(UUID projectId) {
        Project project = projectService.getEntityById(projectId);
        List<TaskDto> taskList = taskService.getAllByProjectId(projectId);
        int completedEasyTasks = 0;
        int completedMediumTasks = 0;
        int completedHardTasks = 0;
        int easyTasks = 0;
        int mediumTasks = 0;
        int hardTasks = 0;
        for (TaskDto taskDto : taskList) {
            switch (taskDto.complexity()) {
                case EASY:
                    easyTasks++;
                    if (taskDto.status().equals(TaskStatus.COMPLETED)) {
                        completedEasyTasks++;
                    }
                    break;

                case MEDIUM:
                    mediumTasks++;
                    if (taskDto.status().equals(TaskStatus.COMPLETED)) {
                        completedMediumTasks++;
                    }
                    break;

                case HARD:
                    hardTasks++;
                    if (taskDto.status().equals(TaskStatus.COMPLETED)) {
                        completedHardTasks++;
                    }
                    break;
            }
        }
        return ProjectStatistics
                .builder()
                .project(project)
                .completedEasyTasks(completedEasyTasks)
                .completedMediumTasks(completedMediumTasks)
                .completedHardTasks(completedHardTasks)
                .easyTasks(easyTasks)
                .mediumTasks(mediumTasks)
                .hardTasks(hardTasks)
                .teamSize(projectMemberService.getUsersByProjectId(projectId).size())
                .daysSinceStart((int) ChronoUnit.DAYS.between(project.getCreatedDate(), LocalDateTime.now()))
                .daysToDeadline((int) ChronoUnit.DAYS.between(LocalDate.now(), project.getDueDate()))
                // Completion probability - Request to NN service
                // Estimated date - Request to NN service
                .savedAt(LocalDateTime.now())
                .build();
    }
}
