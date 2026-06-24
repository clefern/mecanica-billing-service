package com.fiap.mecanica.billing.presentation.dto;

import com.fiap.mecanica.billing.domain.enums.StatusOrcamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrcamentoResponse(
    UUID id,
    String codigo,
    UUID osId,
    UUID sagaId,
    LocalDateTime dataEmissao,
    LocalDateTime dataValidade,
    BigDecimal valorTotalMateriais,
    BigDecimal valorTotalMaoDeObra,
    BigDecimal valorImpostos,
    BigDecimal valorTotal,
    StatusOrcamento status) {
}
