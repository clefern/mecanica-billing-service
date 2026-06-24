package com.fiap.mecanica.billing.domain.model;

import com.fiap.mecanica.billing.domain.enums.StatusPagamento;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pagamento {

  private UUID id;
  private UUID orcamentoId;
  private String mpPreferenceId;
  private String mpPaymentId;
  private String mpInitPoint;
  private StatusPagamento status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
