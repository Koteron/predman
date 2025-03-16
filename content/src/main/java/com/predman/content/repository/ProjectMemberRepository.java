package com.predman.content.repository;

import com.predman.content.entity.ProjectMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    @EntityGraph(attributePaths = {"user"})
    List<ProjectMember> findAllByProject_Id(UUID projectId);

    @EntityGraph(attributePaths = {"project"})
    List<ProjectMember> findAllByUser_Id(UUID projectId);

    @Query("""
    SELECT pm FROM ProjectMember pm
    LEFT JOIN FETCH pm.user
    WHERE pm.project.id IN :projectIds
""")
    List<ProjectMember> findAllByProjectIds(@Param("projectIds") List<UUID> projectIds);
}
