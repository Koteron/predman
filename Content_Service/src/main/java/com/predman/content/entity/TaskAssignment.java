package com.predman.content.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "task_assignments")
public class TaskAssignment {
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@JoinColumn(name = "task_id", referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Task task;

	@Column(name = "joined_at")
	private LocalDateTime joinedAt;
}
