package com.fiap.mecanica.billing.presentation.dto;

import java.util.UUID;

public record SimularPagamentoRequest(
    UUID orcamentoId,
    String decisao) {
}
