package com.fiap.mecanica.billing.infra.messaging.listener;

import com.fiap.mecanica.billing.application.gateway.MercadoPagoGateway;
import com.fiap.mecanica.billing.application.gateway.MercadoPagoGateway.MpPreferenceResult;
import com.fiap.mecanica.billing.application.messaging.FalhaNoBillingEvent;
import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand;
import com.fiap.mecanica.billing.application.messaging.OrcamentoCriadoEvent;
import com.fiap.mecanica.billing.application.messaging.OrcamentoGeradoNotificacaoEvent;
import com.fiap.mecanica.billing.application.messaging.OrcamentoGeradoNotificacaoEvent.ItemOrcamentoNotificacao;
import com.fiap.mecanica.billing.application.service.BillingService;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.infra.messaging.config.RabbitMqConfig;
import com.fiap.mecanica.billing.infra.messaging.publisher.BillingEventPublisher;
import com.fiap.mecanica.billing.infra.messaging.publisher.NotificationEventPublisher;
import com.fiap.mecanica.billing.infra.persistence.entity.ProcessedCommandEntity;
import com.fiap.mecanica.billing.infra.persistence.repository.ProcessedCommandJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingCommandListener {

  private final ProcessedCommandJpaRepository processedRepo;
  private final BillingService billingService;
  private final MercadoPagoGateway mpGateway;
  private final BillingEventPublisher publisher;
  private final NotificationEventPublisher notificationEventPublisher;

  @RabbitListener(queues = RabbitMqConfig.QUEUE_GERAR_ORCAMENTO)
  @Transactional
  public void onGerarOrcamento(GerarOrcamentoCommand command) {
    log.info("[MQ] Recebido GerarOrcamentoCommand sagaId={} osId={}", command.sagaId(), command.osId());

    if (processedRepo.existsBySagaId(command.sagaId())) {
      log.warn("[MQ] Comando já processado sagaId={} — ignorando", command.sagaId());
      return;
    }

    processedRepo.save(ProcessedCommandEntity.builder()
        .id(UUID.randomUUID())
        .sagaId(command.sagaId())
        .processedAt(LocalDateTime.now())
        .build());

    try {
      Orcamento orc = billingService.gerarOrcamento(command);
      MpPreferenceResult mp = mpGateway.criarPreference(orc);
      billingService.salvarPagamento(orc.getId(), mp.preferenceId(), mp.initPoint());

      publisher.publicar(new OrcamentoCriadoEvent(
          command.sagaId(), command.osId(), orc.getId(), mp.initPoint()));

      notificationEventPublisher.publicar(new OrcamentoGeradoNotificacaoEvent(
          orc.getOsId(), orc.getId(), orc.getCodigo(),
          orc.getDataEmissao(), orc.getDataValidade(),
          orc.getValorTotalMateriais(), orc.getValorTotalMaoDeObra(),
          orc.getValorImpostos(), orc.getValorTotal(),
          toItensNotificacao(command.itens())));

    } catch (Exception e) {
      log.error("[BILLING] Erro ao gerar orçamento sagaId={}: {}", command.sagaId(), e.getMessage(), e);
      publisher.publicar(new FalhaNoBillingEvent(
          command.sagaId(), command.osId(), e.getMessage()));
    }
  }

  private List<ItemOrcamentoNotificacao> toItensNotificacao(
      List<GerarOrcamentoCommand.ItemOrcamento> itens) {
    return itens.stream()
        .map(item -> new ItemOrcamentoNotificacao(
            item.descricao(), item.tipo(), item.valorUnitario(), item.quantidade()))
        .toList();
  }
}
