package com.predman.content.entity;


import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "project_statistics")
public class ProjectStatistics {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", referencedColumnName = "id")
	private Project project;

	@Column(name = "team_size")
	private Integer teamSize;

	@Column(name = "days_since_start")
	private Integer daysSinceStart;

	@Column(name = "remaining_tasks")
	private Integer remainingTasks;

	@Column(name = "remaining_story_points")
	private Double remainingStoryPoints;

	@Column(name = "dependency_coefficient")
	private Double dependencyCoefficient;

	@Column(name = "critical_path_length")
	private Double criticalPathLength;

	@Column(name = "sum_experience")
	private Double sumExperience;

	@Column(name = "available_hours")
	private Double availableHours;

	@Column(name = "external_risk_probability")
	private Double externalRiskProbability;

	@Column(name = "saved_at")
	private LocalDateTime savedAt;
}