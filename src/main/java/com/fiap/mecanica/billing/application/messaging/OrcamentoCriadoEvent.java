package com.fiap.mecanica.billing.application.messaging;

import java.util.UUID;

public record OrcamentoCriadoEvent(
    UUID sagaId,
    UUID osId,
    UUID orcamentoId,
    String paymentUrl) {
}
