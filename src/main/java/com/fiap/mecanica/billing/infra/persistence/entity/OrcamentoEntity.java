package com.fiap.mecanica.billing.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fiap.mecanica.billing.domain.enums.StatusOrcamento;

@Entity
@Table(name = "orcamentos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoEntity {

  @Id
  private UUID id;

  @Column(name = "codigo", nullable = false, unique = true)
  private String codigo;

  @Column(name = "os_id", nullable = false)
  private UUID osId;

  @Column(name = "saga_id", nullable = false)
  private UUID sagaId;

  @Column(name = "data_emissao", nullable = false)
  private LocalDateTime dataEmissao;

  @Column(name = "data_validade", nullable = false)
  private LocalDateTime dataValidade;

  @Column(name = "valor_total_materiais", nullable = false, precision = 19, scale = 2)
  private BigDecimal valorTotalMateriais;

  @Column(name = "valor_total_mao_de_obra", nullable = false, precision = 19, scale = 2)
  private BigDecimal valorTotalMaoDeObra;

  @Column(name = "valor_impostos", nullable = false, precision = 19, scale = 2)
  private BigDecimal valorImpostos;

  @Column(name = "valor_total", nullable = false, precision = 19, scale = 2)
  private BigDecimal valorTotal;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private StatusOrcamento status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
