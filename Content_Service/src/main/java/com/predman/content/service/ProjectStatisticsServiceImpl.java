package com.predman.content.service;

import com.predman.content.common.TaskStatus;
import com.predman.content.dto.project_statistics.ProjectStatisticsDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task_dependency.TaskDependencyDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectStatistics;
import com.predman.content.repository.ProjectStatisticsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectStatisticsServiceImpl implements ProjectStatisticsService {
    private final ProjectStatisticsRepository projectStatisticsRepository;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final TaskService taskService;
    private final TaskDependencyService taskDependencyService;

    @Override
    @Transactional
    public List<ProjectStatisticsDto> getProjectStatisticsByProjectId(UUID projectId) {
        updateStatistics(projectId);
        List<ProjectStatistics> projectStatisticsList = projectStatisticsRepository.findAllByProjectId(projectId);
        return projectStatisticsList.stream().map(projectStatistics ->
                ProjectStatisticsDto
                        .builder()
                        .id(projectStatistics.getId())
                        .projectId(projectId)
                        .daysSinceStart(projectStatistics.getDaysSinceStart())
                        .teamSize(projectStatistics.getTeamSize())
                        .remainingTasks(projectStatistics.getRemainingTasks())
                        .remainingStoryPoints(projectStatistics.getRemainingStoryPoints())
                        .dependencyCoefficient(projectStatistics.getDependencyCoefficient())
                        .criticalPathLength(projectStatistics.getCriticalPathLength())
                        .externalRiskProbability(projectStatistics.getExternalRiskProbability())
                        .sumExperience(projectStatistics.getSumExperience())
                        .availableHours(projectStatistics.getAvailableHours())
                        .savedAt(projectStatistics.getSavedAt().toLocalDate())
                        .build()).toList();
    }

    @Override
    @Transactional
    public ProjectStatistics updateStatistics(UUID projectId) {
        ProjectStatistics projectStatistics = projectStatisticsRepository.findLatestStatisticsByProjectId(projectId);
        ProjectStatistics updatedProjectStatistics = reevaluateProjectStatistics(projectId);
        if (projectStatistics.getSavedAt().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
            updatedProjectStatistics.setId(projectStatistics.getId());
        }
        return projectStatisticsRepository.save(updatedProjectStatistics);
    }

    @Override
    @Transactional
    public ProjectStatistics updateStatisticsByUpdatedProject(Project updatedProject) {
        ProjectStatistics projectStatistics =
                projectStatisticsRepository.findLatestStatisticsByProjectId(updatedProject.getId());
        ProjectStatistics updatedProjectStatistics = ProjectStatistics.builder()
                .project(projectStatistics.getProject())
                .criticalPathLength(projectStatistics.getCriticalPathLength())
                .dependencyCoefficient(projectStatistics.getDependencyCoefficient())
                .remainingStoryPoints(projectStatistics.getRemainingStoryPoints())
                .remainingTasks(projectStatistics.getRemainingTasks())
                .availableHours(updatedProject.getAvailableHours())
                .sumExperience(updatedProject.getSumExperience())
                .externalRiskProbability(updatedProject.getExternalRiskProbability())
                .teamSize(projectStatistics.getTeamSize())
                .daysSinceStart((int) ChronoUnit.DAYS.between(updatedProject.getCreatedDate(), LocalDateTime.now()))
                .savedAt(LocalDateTime.now())
                .build();
        if (projectStatistics.getSavedAt().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
            updatedProjectStatistics.setId(projectStatistics.getId());
        }
        return projectStatisticsRepository.save(updatedProjectStatistics);
    }

    @Override
    public void initializeStatistics(Project project) {
        ProjectStatistics projectStatistics = ProjectStatistics
                .builder()
                .project(project)
                .availableHours(0.0)
                .sumExperience(0.0)
                .criticalPathLength(0.0)
                .dependencyCoefficient(0.0)
                .remainingTasks(0)
                .remainingStoryPoints(0.0)
                .teamSize(1)
                .daysSinceStart(0)
                .externalRiskProbability(0.0)
                .savedAt(LocalDateTime.now())
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
        Map<UUID, TaskDto> remainingTaskMap = taskList.stream().filter(taskDto ->
                !taskDto.status().equals(TaskStatus.COMPLETED)).collect(
                        Collectors.toMap(TaskDto::id, taskDto -> taskDto));
        List<TaskDependencyDto> dependencyList = taskDependencyService.getAllProjectDependencies(projectId);

        int dependentTaskNumber = (int)dependencyList.stream()
                .filter(taskDepDto -> remainingTaskMap.containsKey(taskDepDto.taskId())
                        && remainingTaskMap.containsKey(taskDepDto.dependencyId()))
                .map(TaskDependencyDto::taskId)
                .distinct()
                .count();

        Map<UUID, List<UUID>> depsMap = new HashMap<>();
        for (TaskDependencyDto dep : dependencyList) {
            depsMap.computeIfAbsent(dep.taskId(), k -> new ArrayList<>()).add(dep.dependencyId());
        }

        Map<UUID, Double> longestPath = new HashMap<>();

        double remainingStoryPoints = 0;
        for (UUID taskId : remainingTaskMap.keySet()) {
            TaskDto taskDto = remainingTaskMap.get(taskId);

            remainingStoryPoints += taskDto.storyPoints();

            // calculating critical path

            if (taskDto.status().equals(TaskStatus.COMPLETED)) {
                continue;
            }

            List<UUID> taskDeps = depsMap.getOrDefault(taskId, Collections.emptyList());
            double maxDepPath = 0;

            for (UUID depId : taskDeps) {
                TaskDto depTask = remainingTaskMap.get(depId);
                if (depTask != null && !depTask.status().equals(TaskStatus.COMPLETED)
                        && longestPath.containsKey(depId)) {
                    maxDepPath = Math.max(maxDepPath, longestPath.get(depId));
                }
            }

            longestPath.put(taskId, taskDto.storyPoints() + maxDepPath);
        }

        double criticalPathLength = 0;

        if (!longestPath.isEmpty()) {
            for (double pathLength : longestPath.values()) {
                criticalPathLength = Math.max(criticalPathLength, pathLength);
            }
        }

        return ProjectStatistics
                .builder()
                .project(project)
                .availableHours(project.getAvailableHours())
                .sumExperience(project.getSumExperience())
                .externalRiskProbability(project.getExternalRiskProbability())
                .criticalPathLength(criticalPathLength)
                .dependencyCoefficient((double)dependentTaskNumber/remainingTaskMap.size())
                .remainingTasks(remainingTaskMap.size())
                .remainingStoryPoints(remainingStoryPoints)
                .teamSize(projectMemberService.getUsersByProjectId(projectId).size())
                .daysSinceStart((int) ChronoUnit.DAYS.between(project.getCreatedDate(), LocalDateTime.now()))
                .savedAt(LocalDateTime.now())
                .build();
    }
}
