package com.predman.content.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "project_members")
public class ProjectMember {
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@ManyToOne
	private User user;

	@JoinColumn(name = "project_id", referencedColumnName = "id")
	@ManyToOne
	private Project project;

	@Column(name = "joined_at")
	private LocalDateTime joinedAt;
}
