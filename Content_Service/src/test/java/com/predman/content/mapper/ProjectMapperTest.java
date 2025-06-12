package com.predman.content.mapper;

import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.project.ProjectFullInfoDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.User;
import com.predman.content.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMapperTest {

    @Mock UserService userService;
    @Mock UserMapper userMapper;

    @InjectMocks
    ProjectMapper projectMapper;

    UUID userId;
    User user;
    UserDto userDto;
    Project project;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().id(userId).build();
        userDto = UserDto.builder().id(userId).build();
        project = Project.builder()
                .id(UUID.randomUUID())
                .name("Test Project")
                .description("Test Desc")
                .dueDate(LocalDate.of(2025, 6, 30))
                .owner(user)
                .externalRiskProbability(0.2)
                .sumExperience(15.0)
                .availableHours(300.0)
                .certaintyPercent(90.0)
                .predictedDeadline(LocalDate.of(2025, 7, 15))
                .build();
    }

    @Test
    void convertToProjectEntity_shouldMapDtoToEntityCorrectly() {
        ProjectFullInfoDto dto = ProjectFullInfoDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .dueDate(project.getDueDate())
                .ownerId(userId)
                .externalRiskProbability(project.getExternalRiskProbability())
                .sumExperience(project.getSumExperience())
                .availableHours(project.getAvailableHours())
                .build();

        when(userService.getById(userId)).thenReturn(userDto);
        when(userMapper.convertToUserEntity(userDto)).thenReturn(user);

        Project result = projectMapper.convertToProjectEntity(dto);

        assertEquals(dto.id(), result.getId());
        assertEquals(dto.name(), result.getName());
        assertEquals(dto.description(), result.getDescription());
        assertEquals(dto.dueDate(), result.getDueDate());
        assertEquals(dto.externalRiskProbability(), result.getExternalRiskProbability());
        assertEquals(dto.sumExperience(), result.getSumExperience());
        assertEquals(dto.availableHours(), result.getAvailableHours());
        assertEquals(user, result.getOwner());

        verify(userService).getById(userId);
        verify(userMapper).convertToUserEntity(userDto);
    }

    @Test
    void convertToProjectFullInfoDto_shouldMapEntityToFullInfoDtoCorrectly() {
        ProjectFullInfoDto dto = projectMapper.convertToProjectFullInfoDto(project);

        assertEquals(project.getId(), dto.id());
        assertEquals(project.getName(), dto.name());
        assertEquals(project.getDescription(), dto.description());
        assertEquals(project.getDueDate(), dto.dueDate());
        assertEquals(project.getExternalRiskProbability(), dto.externalRiskProbability());
        assertEquals(project.getSumExperience(), dto.sumExperience());
        assertEquals(project.getAvailableHours(), dto.availableHours());
        assertEquals(project.getCertaintyPercent(), dto.certaintyPercent());
        assertEquals(project.getPredictedDeadline(), dto.predictedDeadline());
        assertEquals(userId, dto.ownerId());
    }

    @Test
    void convertToProjectDto_shouldMapEntityToDtoCorrectly() {
        ProjectDto dto = projectMapper.convertToProjectDto(project);

        assertEquals(project.getId(), dto.id());
        assertEquals(project.getName(), dto.name());
        assertEquals(project.getDescription(), dto.description());
        assertEquals(userId, dto.ownerId());
    }
}
