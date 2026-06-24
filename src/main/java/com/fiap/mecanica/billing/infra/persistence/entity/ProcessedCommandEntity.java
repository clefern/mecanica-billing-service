package com.fiap.mecanica.billing.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_commands")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedCommandEntity {

  @Id
  private UUID id;

  @Column(name = "saga_id", nullable = false, unique = true)
  private UUID sagaId;

  @Column(name = "processed_at", nullable = false)
  private LocalDateTime processedAt;
}
