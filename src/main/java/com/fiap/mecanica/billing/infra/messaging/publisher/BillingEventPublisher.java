package com.fiap.mecanica.billing.infra.messaging.publisher;

import com.fiap.mecanica.billing.application.messaging.FalhaNoBillingEvent;
import com.fiap.mecanica.billing.application.messaging.OrcamentoCriadoEvent;
import com.fiap.mecanica.billing.application.messaging.PagamentoConfirmadoEvent;
import com.fiap.mecanica.billing.application.messaging.PagamentoRecusadoEvent;
import com.fiap.mecanica.billing.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publicar(OrcamentoCriadoEvent event) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_ORCAMENTO_CRIADO, event);
    log.info("[MQ] OrcamentoCriadoEvent sagaId={} orcamentoId={}", event.sagaId(), event.orcamentoId());
  }

  public void publicar(FalhaNoBillingEvent event) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_FALHA_BILLING, event);
    log.warn("[MQ] FalhaNoBillingEvent sagaId={} motivo={}", event.sagaId(), event.motivo());
  }

  public void publicar(PagamentoConfirmadoEvent event) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_PAGAMENTO_CONFIRMADO, event);
    log.info("[MQ] PagamentoConfirmadoEvent sagaId={} orcamentoId={}", event.sagaId(), event.orcamentoId());
  }

  public void publicar(PagamentoRecusadoEvent event) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_PAGAMENTO_RECUSADO, event);
    log.warn("[MQ] PagamentoRecusadoEvent sagaId={} motivo={}", event.sagaId(), event.motivo());
  }
}
