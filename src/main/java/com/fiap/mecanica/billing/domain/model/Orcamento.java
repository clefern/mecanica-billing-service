package com.fiap.mecanica.billing.domain.model;

import com.fiap.mecanica.billing.domain.enums.StatusOrcamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Orcamento {

  private UUID id;
  private String codigo;
  private UUID osId;
  private UUID sagaId;
  private LocalDateTime dataEmissao;
  private LocalDateTime dataValidade;
  private BigDecimal valorTotalMateriais;
  private BigDecimal valorTotalMaoDeObra;
  private BigDecimal valorImpostos;
  private BigDecimal valorTotal;
  private StatusOrcamento status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
