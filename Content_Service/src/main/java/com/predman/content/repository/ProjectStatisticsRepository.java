package com.predman.content.repository;

import com.predman.content.entity.ProjectStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectStatisticsRepository extends JpaRepository<ProjectStatistics, UUID> {
    List<ProjectStatistics> findAllByProjectId(UUID projectId);

    @Query(value = """
    SELECT DISTINCT ON (ps.project_id) ps.*
    FROM project_statistics ps
    JOIN projects p ON ps.project_id = p.id
    ORDER BY ps.project_id, ps.saved_at DESC
    """, nativeQuery = true)
    List<ProjectStatistics> findLatestStatisticsForAllProjects();

    @Query(value = """
    SELECT DISTINCT ON (ps.project_id) ps.*
    FROM project_statistics ps
    JOIN projects p ON ps.project_id = p.id
    WHERE ps.project_id = ?1
    ORDER BY ps.project_id, ps.saved_at DESC
    """, nativeQuery = true)
    ProjectStatistics findLatestStatisticsByProjectId(UUID projectId);
}
