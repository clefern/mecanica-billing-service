package com.fiap.mecanica.billing.infra.messaging.publisher;

import com.fiap.mecanica.billing.application.messaging.OrcamentoGeradoNotificacaoEvent;
import com.fiap.mecanica.billing.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publicar(OrcamentoGeradoNotificacaoEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_NOTIFICATION_ORCAMENTO_GERADO, event);
    log.info("[MQ] OrcamentoGeradoNotificacaoEvent orcamentoId={}", event.orcamentoId());
  }
}
