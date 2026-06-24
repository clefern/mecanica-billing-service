package com.fiap.mecanica.billing.application.messaging;

import java.util.UUID;

public record PagamentoConfirmadoEvent(
    UUID sagaId,
    UUID osId,
    UUID orcamentoId,
    String mpPaymentId) {
}
