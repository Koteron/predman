package com.predman.content.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "projects")
public class Project {
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	private String name;

	private String description;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	private LocalDateTime updatedDate;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "certainty_percent")
	private Double certaintyPercent;

	@Column(name = "available_hours")
	private Double availableHours;

	@Column(name = "sum_experience")
	private Double sumExperience;

	@Column(name = "predicted_deadline")
	private LocalDate predictedDeadline;

	@Column(name = "external_risk_probability")
	private Double externalRiskProbability;

	@JoinColumn(name = "owner_id", referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.LAZY)
	private User owner;
}
