package com.fiap.mecanica.billing.infra.messaging.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.billing.application.gateway.MercadoPagoGateway;
import com.fiap.mecanica.billing.application.messaging.FalhaNoBillingEvent;
import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand;
import com.fiap.mecanica.billing.application.messaging.OrcamentoCriadoEvent;
import com.fiap.mecanica.billing.application.service.BillingService;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.infra.messaging.publisher.BillingEventPublisher;
import com.fiap.mecanica.billing.infra.persistence.repository.ProcessedCommandJpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingCommandListenerTest {

  @Mock ProcessedCommandJpaRepository processedRepo;
  @Mock BillingService billingService;
  @Mock MercadoPagoGateway mpGateway;
  @Mock BillingEventPublisher publisher;

  @InjectMocks BillingCommandListener listener;

  @Test
  void onGerarOrcamento_novoComando_deveProcessarEPublicarOrcamentoCriado() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID orcId = UUID.randomUUID();
    GerarOrcamentoCommand command = new GerarOrcamentoCommand(sagaId, osId, List.of());

    Orcamento orcamento = Orcamento.builder()
        .id(orcId)
        .codigo("ORC-TEST")
        .osId(osId)
        .sagaId(sagaId)
        .valorTotal(BigDecimal.TEN)
        .build();

    when(processedRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    when(billingService.gerarOrcamento(command)).thenReturn(orcamento);
    when(mpGateway.criarPreference(orcamento))
        .thenReturn(new MercadoPagoGateway.MpPreferenceResult("pref-123", "http://mp/checkout"));
    when(billingService.salvarPagamento(any(), any(), any())).thenReturn(null);

    listener.onGerarOrcamento(command);

    verify(billingService).gerarOrcamento(command);
    verify(mpGateway).criarPreference(orcamento);
    verify(publisher).publicar(argThat((OrcamentoCriadoEvent e) ->
        e.sagaId().equals(sagaId) && e.osId().equals(osId) && e.orcamentoId().equals(orcId)));
    verify(publisher, never()).publicar(any(FalhaNoBillingEvent.class));
  }

  @Test
  void onGerarOrcamento_comandoDuplicado_deveIgnorar() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    GerarOrcamentoCommand command = new GerarOrcamentoCommand(sagaId, osId, List.of());

    when(processedRepo.existsBySagaId(sagaId)).thenReturn(true);

    listener.onGerarOrcamento(command);

    verify(billingService, never()).gerarOrcamento(any());
    verify(publisher, never()).publicar(any(OrcamentoCriadoEvent.class));
    verify(publisher, never()).publicar(any(FalhaNoBillingEvent.class));
  }

  @Test
  void onGerarOrcamento_erroNoGateway_devePublicarFalha() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    GerarOrcamentoCommand command = new GerarOrcamentoCommand(sagaId, osId, List.of());

    Orcamento orcamento = Orcamento.builder()
        .id(UUID.randomUUID())
        .codigo("ORC-TEST")
        .osId(osId)
        .sagaId(sagaId)
        .valorTotal(BigDecimal.TEN)
        .build();

    when(processedRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    when(billingService.gerarOrcamento(command)).thenReturn(orcamento);
    when(mpGateway.criarPreference(any())).thenThrow(new RuntimeException("timeout MP"));

    listener.onGerarOrcamento(command);

    verify(publisher, never()).publicar(any(OrcamentoCriadoEvent.class));
    verify(publisher).publicar(argThat((FalhaNoBillingEvent e) -> e.sagaId().equals(sagaId)));
  }
}
