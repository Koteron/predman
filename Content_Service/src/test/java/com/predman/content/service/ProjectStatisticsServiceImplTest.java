package com.predman.content.service;

import com.predman.content.common.TaskStatus;
import com.predman.content.dto.project_statistics.ProjectStatisticsDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task_dependency.TaskDependencyDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectStatistics;
import com.predman.content.entity.Task;
import com.predman.content.entity.User;
import com.predman.content.repository.ProjectStatisticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectStatisticsServiceImplTest {
    @Mock private ProjectStatisticsRepository projectStatisticsRepository;
    @Mock private ProjectService projectService;
    @Mock private ProjectMemberService projectMemberService;
    @Mock private TaskService taskService;
    @Mock private TaskDependencyService taskDependencyService;

    @InjectMocks
    private ProjectStatisticsServiceImpl projectStatisticsService;

    private static final UUID PROJECT_ID = UUID.randomUUID();

    @Test
    void initializeStatistics_savesDefaultStatistics() {
        Project project = Project.builder().id(PROJECT_ID).build();

        projectStatisticsService.initializeStatistics(project);

        ArgumentCaptor<ProjectStatistics> captor = ArgumentCaptor.forClass(ProjectStatistics.class);
        verify(projectStatisticsRepository).save(captor.capture());

        ProjectStatistics saved = captor.getValue();
        assertEquals(PROJECT_ID, saved.getProject().getId());
        assertEquals(0.0, saved.getAvailableHours());
        assertEquals(1, saved.getTeamSize());
        assertEquals(0, saved.getRemainingTasks());
    }

    @Test
    void getAllLatestStatistics_returnsAll() {
        List<ProjectStatistics> list = List.of(ProjectStatistics.builder().build());
        when(projectStatisticsRepository.findLatestStatisticsForAllProjects()).thenReturn(list);

        List<ProjectStatistics> result = projectStatisticsService.getAllLatestStatistics();
        assertEquals(list, result);
    }

    @Test
    void updateStatistics_updatesIfAlreadySavedToday() {
        ProjectStatistics existing = ProjectStatistics.builder()
                .savedAt(LocalDateTime.now())
                .project(Project.builder().createdDate(
                        LocalDateTime.of(2023, 1, 1, 0, 0))
                        .id(PROJECT_ID).build())
                .build();
        when(projectStatisticsRepository.findLatestStatisticsByProjectId(PROJECT_ID)).thenReturn(existing);
        when(projectService.getEntityById(PROJECT_ID)).thenReturn(existing.getProject());
        when(projectMemberService.getUsersByProjectId(PROJECT_ID)).thenReturn(List.of(UserDto.builder().build()));
        when(taskService.getAllByProjectId(PROJECT_ID)).thenReturn(Collections.emptyList());
        when(taskDependencyService.getAllProjectDependencies(PROJECT_ID)).thenReturn(Collections.emptyList());

        projectStatisticsService.updateStatistics(PROJECT_ID);

        verify(projectStatisticsRepository).save(any(ProjectStatistics.class));
    }

    @Test
    void updateStatisticsByUpdatedProject_copiesValuesAndSaves() {
        Project updatedProject = Project.builder()
                .id(PROJECT_ID)
                .availableHours(10.0)
                .sumExperience(5.0)
                .externalRiskProbability(0.1)
                .createdDate(LocalDateTime.now().minusDays(5))
                .build();

        ProjectStatistics latest = ProjectStatistics.builder()
                .savedAt(LocalDateTime.now())
                .project(updatedProject)
                .remainingTasks(3)
                .remainingStoryPoints(13.0)
                .criticalPathLength(8.0)
                .dependencyCoefficient(0.6)
                .teamSize(3)
                .build();

        when(projectStatisticsRepository.findLatestStatisticsByProjectId(PROJECT_ID)).thenReturn(latest);

        projectStatisticsService.updateStatisticsByUpdatedProject(updatedProject);

        verify(projectStatisticsRepository).save(argThat(updated ->
                updated.getAvailableHours() == 10.0 &&
                        updated.getSumExperience() == 5.0 &&
                        updated.getExternalRiskProbability() == 0.1 &&
                        updated.getDaysSinceStart() == 5 &&
                        updated.getRemainingTasks() == 3
        ));
    }

    @Test
    void getProjectStatisticsByProjectId_callsUpdateAndMaps() {
        ProjectStatistics stat = ProjectStatistics.builder()
                .id(UUID.randomUUID())
                .project(Project.builder().createdDate(
                        LocalDateTime.of(2023, 1, 1, 0, 0))
                        .id(PROJECT_ID).build())
                .teamSize(3)
                .remainingTasks(5)
                .remainingStoryPoints(8.0)
                .dependencyCoefficient(0.2)
                .criticalPathLength(7.0)
                .externalRiskProbability(0.1)
                .sumExperience(4.0)
                .availableHours(6.0)
                .daysSinceStart(10)
                .savedAt(LocalDateTime.now())
                .build();

        when(projectStatisticsRepository.findLatestStatisticsByProjectId(PROJECT_ID)).thenReturn(stat);
        when(projectStatisticsRepository.findAllByProjectId(PROJECT_ID)).thenReturn(List.of(stat));
        when(projectService.getEntityById(PROJECT_ID)).thenReturn(stat.getProject());
        when(taskService.getAllByProjectId(PROJECT_ID)).thenReturn(List.of(
                TaskDto.builder().id(UUID.fromString("11111111-2222-1111-1111-111111111111"))
                        .status(TaskStatus.PLANNED).storyPoints(5.0).build(),
                TaskDto.builder().id(UUID.fromString("11111111-3333-1111-1111-111111111111"))
                        .status(TaskStatus.IN_PROGRESS).storyPoints(10.0).build(),
                TaskDto.builder().id(UUID.randomUUID()).status(TaskStatus.COMPLETED).storyPoints(15.0).build()
        ));
        when(taskDependencyService.getAllProjectDependencies(PROJECT_ID)).thenReturn(List.of(
                TaskDependencyDto.builder()
                        .taskId(UUID.fromString("11111111-2222-1111-1111-111111111111"))
                        .dependencyId(UUID.fromString("11111111-3333-1111-1111-111111111111"))
                        .build()
        ));
        when(projectMemberService.getUsersByProjectId(PROJECT_ID)).thenReturn(List.of(UserDto.builder().build()));

        List<ProjectStatisticsDto> result = projectStatisticsService.getProjectStatisticsByProjectId(PROJECT_ID);

        assertEquals(1, result.size());
        assertEquals(PROJECT_ID, result.getFirst().projectId());
    }
}