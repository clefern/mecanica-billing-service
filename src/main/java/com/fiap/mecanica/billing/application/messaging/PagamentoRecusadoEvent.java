package com.fiap.mecanica.billing.application.messaging;

import java.util.UUID;

public record PagamentoRecusadoEvent(
    UUID sagaId,
    UUID osId,
    UUID orcamentoId,
    String motivo) {
}
