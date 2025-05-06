package com.predman.content.repository;

import com.predman.content.entity.TaskDependency;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {
    @EntityGraph(attributePaths = {"task"})
    List<TaskDependency> findAllByTaskId(UUID taskId);

    @Query(value = """
    DELETE FROM task_dependency
    WHERE task_id = :taskId
    AND dependency_id = :dependencyId
    """, nativeQuery = true)
    void deleteByIdPair(UUID taskId, UUID dependencyId);

    @Query(value = """
    SELECT td.*
    FROM task_dependencies td
    JOIN tasks t ON t.id = td.task_id
    WHERE project_id = ?1
    """, nativeQuery = true)
    List<TaskDependency> findAllByProjectId(UUID projectId);
}
