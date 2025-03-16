package com.predman.content.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;
  
  private String login;
  
  @Column(name = "password_hash")
  private String passwordHash;

  private String email;
  
  @Column(name = "created_date")
  private LocalDateTime createdDate;
  
  @Column(name = "updated_date")
  private LocalDateTime updatedDate;
}
