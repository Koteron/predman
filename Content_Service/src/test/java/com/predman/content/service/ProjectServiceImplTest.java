package com.predman.content.service;

import com.predman.content.dto.grpc.PredictionDto;
import com.predman.content.dto.project.*;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.ProjectMapper;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    @Mock private ProjectMemberService projectMemberService;
    @Mock private EntityManager entityManager;
    @Mock private UserService userService;
    @Mock private ProjectStatisticsService projectStatisticsService;
    @Mock private TaskService taskService;
    @Mock private StatisticsService statisticsService;
    @Mock private UserMapper userMapper;

    @Spy
    @InjectMocks
    private ProjectServiceImpl projectService;

    private final UUID PROJECT_ID = UUID.randomUUID();

    private final User OWNER = User.builder().id(UUID.randomUUID()).build();
    private final UserDto OWNER_DTO = new UserDto(OWNER.getId(), "email", "login");

    private final Project PROJECT = Project.builder()
            .id(PROJECT_ID)
            .name("Old")
            .description("Old Desc")
            .dueDate(LocalDate.now().plusDays(5))
            .createdDate(LocalDateTime.now().minusDays(5))
            .updatedDate(LocalDateTime.now().minusDays(5))
            .owner(OWNER)
            .build();

    private final ProjectCreationDto CREATION_DTO = ProjectCreationDto.builder()
            .name("NewName")
            .description("NewDesc")
            .dueDate(LocalDate.now().plusDays(10))
            .build();

    private final ProjectUpdateDto UPDATE_DTO_VALID = ProjectUpdateDto.builder()
            .name("NewName2")
            .availableHours(10.0)
            .sumExperience(20.0)
            .externalRiskProbability(0.2)
            .dueDate(LocalDate.now().plusDays(15))
            .description("New Desc2")
            .build();

    @Test
    void create_savesProject_andAddsMemberAndStats() {
        Project saved = Project.builder()
                .id(PROJECT_ID)
                .name(CREATION_DTO.name())
                .description(CREATION_DTO.description())
                .dueDate(CREATION_DTO.dueDate())
                .owner(OWNER)
                .build();
        ProjectDto dto = ProjectDto.builder().id(saved.getId()).build();

        when(userService.getAuthenticatedUser()).thenReturn(OWNER);
        when(projectRepository.save(any())).thenReturn(saved);
        when(projectMemberService.addProjectMember(OWNER, saved))
                .thenReturn(UserDto.builder().id(OWNER.getId()).build());
        doNothing().when(projectStatisticsService).initializeStatistics(saved);
        when(projectMapper.convertToProjectDto(saved)).thenReturn(dto);

        ProjectDto result = projectService.create(CREATION_DTO);

        assertEquals(saved.getId(), result.id());
        verify(projectMemberService).addProjectMember(OWNER, saved);
        verify(projectStatisticsService).initializeStatistics(saved);
    }

    @Test
    void update_throwsOnEmptyName() {
        ProjectUpdateDto bad = ProjectUpdateDto.builder()
                .name("") // empty
                .build();
        assertThrows(ForbiddenException.class, () -> projectService.update(PROJECT, bad));
    }

    @Test
    void update_updatesStatsAndPrediction_whenFieldsChanged() {
        PredictionDto pred = PredictionDto.builder()
                .predictedDays(7).certaintyPercent(0.75).build();
        Project updated = Project.builder()
                .id(PROJECT_ID)
                .name(UPDATE_DTO_VALID.name())
                .description(UPDATE_DTO_VALID.description())
                .dueDate(UPDATE_DTO_VALID.dueDate())
                .owner(OWNER)
                .build();
        ProjectFullInfoDto fullDto = ProjectFullInfoDto.builder().id(PROJECT_ID).build();

        when(projectStatisticsService.updateStatisticsByUpdatedProject(any())).thenReturn(null);
        when(statisticsService.getPrediction(eq(PROJECT_ID), anyInt())).thenReturn(pred);
        when(projectRepository.save(any())).thenReturn(updated);
        when(projectMapper.convertToProjectFullInfoDto(updated)).thenReturn(fullDto);

        ProjectFullInfoDto result = projectService.update(PROJECT, UPDATE_DTO_VALID);

        assertEquals(PROJECT_ID, result.id());
        verify(statisticsService).getPrediction(eq(PROJECT_ID), anyInt());
    }

    @Test
    void updatePrediction_usesDefaultIfException() {
        when(statisticsService.getPrediction(eq(PROJECT_ID), anyInt()))
                .thenThrow(new RuntimeException());
        ProjectFullInfoDto dto = ProjectFullInfoDto.builder().id(PROJECT_ID).build();
        Project saved = Project.builder().id(PROJECT_ID).build();

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.convertToProjectFullInfoDto(any(Project.class))).thenAnswer(invocation -> {
            Project input = invocation.getArgument(0);
            return ProjectFullInfoDto
                    .builder()
                    .id(input.getId())
                    .certaintyPercent(input.getCertaintyPercent())
                    .build();
            }
        );

        ProjectFullInfoDto result = projectService.updatePrediction(PROJECT);
        assertEquals(PROJECT_ID, result.id());
        assertEquals(0.0, result.certaintyPercent());
    }

    @Test
    void getFullInfoById_notFound_throws() {
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> projectService.getFullInfoById(PROJECT_ID));
    }

    @Test
    void getWithTaskListById_returnsTasks() {
        List<TaskDto> tasks = List.of(TaskDto.builder().id(UUID.randomUUID()).build());
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(PROJECT));
        when(taskService.getAllByProjectId(PROJECT_ID)).thenReturn(tasks);

        ProjectTaskListDto dto = projectService.getWithTaskListById(PROJECT_ID);
        assertEquals(PROJECT_ID, dto.id());
        assertEquals(tasks, dto.tasks());
    }

    @Test
    void getEntityById_throwsNotFound() {
        when(entityManager.find(Project.class, PROJECT_ID)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> projectService.getEntityById(PROJECT_ID));
    }

    @Test
    void getAllByOwnerId_mapsList() {
        when(projectRepository.findByOwner_Id(OWNER.getId())).thenReturn(List.of(PROJECT));
        ProjectDto dto = ProjectDto.builder().id(PROJECT_ID).build();
        when(projectMapper.convertToProjectDto(PROJECT)).thenReturn(dto);

        List<ProjectDto> result = projectService.getAllByOwnerId(OWNER.getId());
        assertEquals(1, result.size());
    }

    @Test
    void changeOwnerChecked_throwsWhenNotMember() {
        Project p = PROJECT;
        when(userService.getEntityByEmail("other@example.com")).thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(projectMemberService.getUsersByProjectId(PROJECT_ID)).thenReturn(List.of(OWNER_DTO));
        assertThrows(NotFoundException.class,
                () -> projectService.changeOwnerChecked(p, "other@example.com"));
    }

    @Test
    void changeOwnerRandom_deletesIfNoAlternatives() {
        when(projectMemberService.getUsersByProjectId(PROJECT_ID)).thenReturn(List.of(OWNER_DTO));
        projectService.changeOwnerRandom(OWNER.getId(), PROJECT_ID);
        verify(projectRepository).deleteById(PROJECT_ID);
    }

    @Test
    void changeOwnerRandom_reassignsIfAlternatives() {
        UserDto alt = new UserDto(UUID.randomUUID(), "email2", "login2");
        when(projectMemberService.getUsersByProjectId(PROJECT_ID)).thenReturn(List.of(OWNER_DTO, alt));
        when(userMapper.convertToUserEntity(alt)).thenReturn(User.builder().id(alt.id()).build());
        Project mock = PROJECT;
        when(entityManager.find(Project.class, PROJECT_ID)).thenReturn(mock);
        when(projectRepository.save(any())).thenReturn(mock);

        projectService.changeOwnerRandom(OWNER.getId(), PROJECT_ID);
        verify(projectRepository).save(mock);
    }

    @Test
    void getById_returnsProjectDto() {
        ProjectDto expectedDto = ProjectDto.builder().id(PROJECT_ID).build();
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(PROJECT));
        when(projectMapper.convertToProjectDto(PROJECT)).thenReturn(expectedDto);

        ProjectDto result = projectService.getById(PROJECT_ID);

        assertEquals(expectedDto.id(), result.id());
    }

    @Test
    void getAllEntitiesByOwnerId_returnsListOfProjects() {
        when(projectRepository.findByOwner_Id(OWNER.getId())).thenReturn(List.of(PROJECT));

        List<Project> result = projectService.getAllEntitiesByOwnerId(OWNER.getId());

        assertEquals(1, result.size());
        assertEquals(PROJECT_ID, result.getFirst().getId());
    }

    @Test
    void delete_deletesById() {
        projectService.delete(PROJECT);
        verify(projectRepository).deleteById(PROJECT_ID);
    }

    @Test
    void changeOwnerChecked_changesOwnerWhenUserIsMember() {
        UserDto newOwnerDto = new UserDto(UUID.randomUUID(), "new@email.com", "newlogin");
        User newOwner = User.builder().id(newOwnerDto.id()).build();

        when(userService.getEntityByEmail("new@email.com")).thenReturn(newOwner);
        when(projectMemberService.getUsersByProjectId(PROJECT_ID)).thenReturn(List.of(OWNER_DTO, newOwnerDto));
        ProjectDto expectedDto = ProjectDto.builder().id(PROJECT_ID).build();

        when(userService.getEntityByEmail("new@email.com")).thenReturn(newOwner);
        doReturn(expectedDto).when(projectService).changeOwnerUnchecked(PROJECT, newOwner);

        ProjectDto result = projectService.changeOwnerChecked(PROJECT, "new@email.com");

        assertEquals(PROJECT_ID, result.id());
    }

    @Test
    void getFullInfoById_returnsFullInfo_whenProjectExists() {
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(PROJECT));
        ProjectFullInfoDto expected = ProjectFullInfoDto.builder().id(PROJECT_ID).build();

        doReturn(expected).when(projectService).updatePrediction(PROJECT);

        ProjectFullInfoDto result = projectService.getFullInfoById(PROJECT_ID);

        assertEquals(expected.id(), result.id());
    }

    @Test
    void update_setsDefaults_whenAllFieldsNull() {
        ProjectUpdateDto dto = ProjectUpdateDto.builder().build();

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.convertToProjectFullInfoDto(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project p = invocation.getArgument(0);
                    return ProjectFullInfoDto.builder()
                            .id(p.getId())
                            .certaintyPercent(p.getCertaintyPercent())
                            .predictedDeadline(p.getPredictedDeadline())
                            .build();
                });

        ProjectFullInfoDto result = projectService.update(PROJECT, dto);

        assertEquals(PROJECT.getId(), result.id());
        assertEquals(PROJECT.getCertaintyPercent(), result.certaintyPercent());
        assertEquals(PROJECT.getPredictedDeadline(), result.predictedDeadline());
        verify(projectStatisticsService, never()).updateStatisticsByUpdatedProject(any());
        verify(statisticsService, never()).getPrediction(any(), anyInt());
    }

    @Test
    void update_triggersPrediction_whenDueDateProvided() {
        LocalDate newDue = LocalDate.now().plusDays(20);
        ProjectUpdateDto dto = ProjectUpdateDto.builder()
                .dueDate(newDue)
                .build();

        PredictionDto prediction = PredictionDto.builder()
                .predictedDays(10)
                .certaintyPercent(0.5)
                .build();

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.convertToProjectFullInfoDto(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project p = invocation.getArgument(0);
                    return ProjectFullInfoDto.builder()
                            .id(p.getId())
                            .certaintyPercent(p.getCertaintyPercent())
                            .predictedDeadline(p.getPredictedDeadline())
                            .build();
                });

        when(statisticsService.getPrediction(eq(PROJECT_ID), anyInt())).thenReturn(prediction);

        ProjectFullInfoDto result = projectService.update(PROJECT, dto);

        assertEquals(PROJECT_ID, result.id());
        assertEquals(0.5, result.certaintyPercent());
        assertEquals(PROJECT.getCreatedDate().toLocalDate().plusDays(10), result.predictedDeadline());
        verify(projectStatisticsService).updateStatisticsByUpdatedProject(any());
    }

}