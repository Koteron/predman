package com.predman.content.entity;

import java.util.UUID;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import com.predman.content.common.TaskStatus;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "tasks")
public class Task {
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@JoinColumn(name = "project_id", referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Project project;

	private String name;

	private String description;

	private Double storyPoints;

	@Enumerated(EnumType.STRING)
	private TaskStatus status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}