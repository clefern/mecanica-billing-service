package com.fiap.mecanica.billing.infra.mercadopago;

import com.fiap.mecanica.billing.application.gateway.MercadoPagoGateway;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MercadoPagoAdapter implements MercadoPagoGateway {

  @Value("${mercadopago.access-token}")
  private String accessToken;

  @PostConstruct
  void init() {
    MercadoPagoConfig.setAccessToken(accessToken);
    log.info("[MP] MercadoPago SDK configurado");
  }

  @Override
  public MpPreferenceResult criarPreference(Orcamento orc) {
    try {
      PreferenceItemRequest item = PreferenceItemRequest.builder()
          .id(orc.getId().toString())
          .title("Ordem de Serviço " + orc.getCodigo())
          .description("Reparo veicular — " + orc.getCodigo())
          .quantity(1)
          .unitPrice(orc.getValorTotal())
          .currencyId("BRL")
          .build();

      PreferenceRequest request = PreferenceRequest.builder()
          .items(List.of(item))
          .externalReference(orc.getId().toString())
          .build();

      PreferenceClient client = new PreferenceClient();
      Preference preference = client.create(request);

      log.info("[MP] Preference criada id={} initPoint={}", preference.getId(), preference.getInitPoint());
      return new MpPreferenceResult(preference.getId(), preference.getInitPoint());

    } catch (Exception e) {
      log.error("[MP] Erro ao criar Preference orcamentoId={}: {}", orc.getId(), e.getMessage());
      // Em sandbox sem token válido, retorna URL simulada para não travar o fluxo
      String fakeUrl = "https://sandbox.mercadopago.com.br/checkout/v1/redirect?pref_id=SIMULADO-" + orc.getId();
      log.warn("[MP] Usando URL simulada: {}", fakeUrl);
      return new MpPreferenceResult("SIMULADO-" + orc.getId(), fakeUrl);
    }
  }
}
