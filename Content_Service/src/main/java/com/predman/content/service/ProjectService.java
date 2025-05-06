package com.predman.content.service;

import com.predman.content.dto.project.ProjectCreationDto;
import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.project.ProjectFullInfoDto;
import com.predman.content.dto.project.ProjectUpdateDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.User;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    Project getEntityById(UUID id);
    ProjectDto create(ProjectCreationDto projectCreationDto);
    ProjectFullInfoDto update(Project project, ProjectUpdateDto projectUpdateDto);
    ProjectFullInfoDto updatePrediction(Project project);
    ProjectDto getById(UUID id);
    ProjectFullInfoDto getFullInfoById(UUID projectId);
    List<ProjectDto> getAllByOwnerId(UUID ownerId);
    List<Project> getAllEntitiesByOwnerId(UUID ownerId);
    void delete(Project project);
    ProjectDto changeOwnerChecked(Project project, String newOwnerEmail);
    ProjectDto changeOwnerUnchecked(Project project, User newOwner);


}
