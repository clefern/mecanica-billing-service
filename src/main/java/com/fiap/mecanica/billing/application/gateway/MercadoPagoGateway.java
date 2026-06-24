package com.fiap.mecanica.billing.application.gateway;

import com.fiap.mecanica.billing.domain.model.Orcamento;

public interface MercadoPagoGateway {

  MpPreferenceResult criarPreference(Orcamento orcamento);

  record MpPreferenceResult(String preferenceId, String initPoint) {
  }
}
