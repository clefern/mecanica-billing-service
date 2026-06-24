package com.fiap.mecanica.billing.presentation.controller;

import com.fiap.mecanica.billing.application.messaging.PagamentoConfirmadoEvent;
import com.fiap.mecanica.billing.application.messaging.PagamentoRecusadoEvent;
import com.fiap.mecanica.billing.application.port.out.OrcamentoRepositoryPort;
import com.fiap.mecanica.billing.application.service.BillingService;
import com.fiap.mecanica.billing.domain.exception.OrcamentoNaoEncontradoException;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.infra.messaging.publisher.BillingEventPublisher;
import com.fiap.mecanica.billing.presentation.dto.SimularPagamentoRequest;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/billing/webhooks")
@RequiredArgsConstructor
public class WebhookController {

  private final BillingService billingService;
  private final OrcamentoRepositoryPort orcamentoRepository;
  private final BillingEventPublisher publisher;

  @PostMapping("/mercadopago")
  public ResponseEntity<Void> mercadoPago(@RequestBody Map<String, Object> payload) {
    log.info("[WEBHOOK] Recebido payload MP: {}", payload);
    String type = (String) payload.get("type");
    if (!"payment".equals(type)) {
      return ResponseEntity.ok().build();
    }

    Map<?, ?> data = (Map<?, ?>) payload.get("data");
    if (data == null) {
      return ResponseEntity.badRequest().build();
    }

    String mpPaymentId = String.valueOf(data.get("id"));
    log.info("[WEBHOOK] Notificação de pagamento mpPaymentId={}", mpPaymentId);

    // Em implementação real: consultar MP GET /v1/payments/{id} e verificar status.
    // Para sandbox: o endpoint /simular é o caminho de teste sem ngrok.

    return ResponseEntity.ok().build();
  }

  @PostMapping("/simular")
  public ResponseEntity<Void> simular(@RequestBody SimularPagamentoRequest request) {
    log.info("[WEBHOOK] Simulando pagamento orcamentoId={} decisao={}", request.orcamentoId(), request.decisao());

    Orcamento orc = orcamentoRepository.buscarPorId(request.orcamentoId())
        .orElseThrow(() -> new OrcamentoNaoEncontradoException(request.orcamentoId()));

    if ("APROVADO".equalsIgnoreCase(request.decisao())) {
      billingService.aprovarPagamento(orc.getId(), "SIMULADO-" + UUID.randomUUID());
      publisher.publicar(new PagamentoConfirmadoEvent(
          orc.getSagaId(), orc.getOsId(), orc.getId(), "SIMULADO"));

    } else if ("RECUSADO".equalsIgnoreCase(request.decisao())) {
      billingService.recusarPagamento(orc.getId(), "Pagamento recusado via simulação");
      publisher.publicar(new PagamentoRecusadoEvent(
          orc.getSagaId(), orc.getOsId(), orc.getId(), "Pagamento recusado via simulação"));

    } else {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok().build();
  }
}
