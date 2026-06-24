package com.fiap.mecanica.billing.application.messaging;

import java.util.UUID;

public record FalhaNoBillingEvent(
    UUID sagaId,
    UUID osId,
    String motivo) {
}
