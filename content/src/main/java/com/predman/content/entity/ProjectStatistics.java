package com.predman.content.entity;


import java.time.LocalDate;
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

	@Column(name = "easy_tasks")
	private Integer easyTasks;

	@Column(name = "medium_tasks")
	private Integer mediumTasks;

	@Column(name = "hard_tasks")
	private Integer hardTasks;

	@Column(name = "completed_easy_tasks")
	private Integer completedEasyTasks;

	@Column(name = "completed_medium_tasks")
	private Integer completedMediumTasks;

	@Column(name = "completed_hard_tasks")
	private Integer completedHardTasks;

	@Column(name = "team_size")
	private Integer teamSize;

	@Column(name = "days_since_start")
	private Integer daysSinceStart;

	@Column(name = "days_to_deadline")
	private Integer daysToDeadline;

	@Column(name = "completion_probability")
	private Double completionProbability;

	@Column(name = "estimated_date")
	private LocalDate estimatedDate;

	@Column(name = "saved_at")
	private LocalDateTime savedAt;
}
