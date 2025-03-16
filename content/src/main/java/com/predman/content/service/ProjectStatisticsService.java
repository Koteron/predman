package com.predman.content.service;

import com.predman.content.dto.project_statistics.ProjectStatisticsDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectStatistics;

import java.util.List;
import java.util.UUID;

public interface ProjectStatisticsService {
    List<ProjectStatisticsDto> getProjectStatisticsByProjectId(UUID projectId);
    ProjectStatistics updateStatistics(UUID projectId);
    void initializeStatistics(Project project);
    List<ProjectStatistics> getAllLatestStatistics();
}
