package com.predman.content.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "task_dependencies")
public class TaskDependency {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @JoinColumn(name = "task_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    @JoinColumn(name = "dependency_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Task dependency;
}
