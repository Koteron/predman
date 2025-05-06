package com.predman.content.repository;

import com.predman.content.entity.TaskAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {
    @EntityGraph(attributePaths = {"task"})
    List<TaskAssignment> findAllByUser_Id(UUID userId);

    @EntityGraph(attributePaths = {"user"})
    List<TaskAssignment> findAllByTask_Id(UUID taskId);

    void deleteAllByUser_Id(UUID userId);
}
