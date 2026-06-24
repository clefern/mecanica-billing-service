package com.fiap.mecanica.billing.application.service;

import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand;
import com.fiap.mecanica.billing.application.port.out.OrcamentoRepositoryPort;
import com.fiap.mecanica.billing.application.port.out.PagamentoRepositoryPort;
import com.fiap.mecanica.billing.domain.enums.StatusOrcamento;
import com.fiap.mecanica.billing.domain.enums.StatusPagamento;
import com.fiap.mecanica.billing.domain.exception.OrcamentoNaoEncontradoException;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.domain.model.Pagamento;
import com.fiap.mecanica.billing.domain.service.OrcamentoCalculoService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

  private final OrcamentoRepositoryPort orcamentoRepository;
  private final PagamentoRepositoryPort pagamentoRepository;
  private final OrcamentoCalculoService calculoService;

  @Value("${mecanica.orcamento.validade-dias:30}")
  private int validadeDias;

  @Transactional
  public Orcamento gerarOrcamento(GerarOrcamentoCommand command) {
    OrcamentoCalculoService.ResultadoCalculo calculo = calculoService.calcular(command.itens());
    LocalDateTime agora = LocalDateTime.now();

    Orcamento orcamento = Orcamento.builder()
        .id(UUID.randomUUID())
        .codigo(gerarCodigo(agora))
        .osId(command.osId())
        .sagaId(command.sagaId())
        .dataEmissao(agora)
        .dataValidade(agora.plusDays(validadeDias))
        .valorTotalMateriais(calculo.totalMateriais())
        .valorTotalMaoDeObra(calculo.totalMaoDeObra())
        .valorImpostos(calculo.valorImpostos())
        .valorTotal(calculo.valorTotal())
        .status(StatusOrcamento.GERADO)
        .build();

    Orcamento salvo = orcamentoRepository.salvar(orcamento);
    log.info("[BILLING] Orçamento criado id={} codigo={} total={}", salvo.getId(), salvo.getCodigo(), salvo.getValorTotal());
    return salvo;
  }

  @Transactional
  public Pagamento salvarPagamento(UUID orcamentoId, String preferenceId, String initPoint) {
    Pagamento pagamento = Pagamento.builder()
        .id(UUID.randomUUID())
        .orcamentoId(orcamentoId)
        .mpPreferenceId(preferenceId)
        .mpInitPoint(initPoint)
        .status(StatusPagamento.PENDENTE)
        .build();
    return pagamentoRepository.salvar(pagamento);
  }

  @Transactional
  public Pagamento aprovarPagamento(UUID orcamentoId, String mpPaymentId) {
    Pagamento pagamento = buscarPagamentoPorOrcamento(orcamentoId);
    pagamento.setStatus(StatusPagamento.APROVADO);
    pagamento.setMpPaymentId(mpPaymentId);
    return pagamentoRepository.salvar(pagamento);
  }

  @Transactional
  public Pagamento recusarPagamento(UUID orcamentoId, String motivo) {
    Pagamento pagamento = buscarPagamentoPorOrcamento(orcamentoId);
    pagamento.setStatus(StatusPagamento.RECUSADO);
    return pagamentoRepository.salvar(pagamento);
  }

  @Transactional(readOnly = true)
  public Orcamento buscarPorId(UUID id) {
    return orcamentoRepository.buscarPorId(id)
        .orElseThrow(() -> new OrcamentoNaoEncontradoException(id));
  }

  @Transactional(readOnly = true)
  public Page<Orcamento> listarTodas(Pageable pageable) {
    return orcamentoRepository.listarTodas(pageable);
  }

  private Pagamento buscarPagamentoPorOrcamento(UUID orcamentoId) {
    return pagamentoRepository.buscarPorOrcamentoId(orcamentoId)
        .orElseThrow(() -> new OrcamentoNaoEncontradoException(orcamentoId));
  }

  private String gerarCodigo(LocalDateTime agora) {
    String data = agora.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String sufixo = String.valueOf(agora.toLocalTime().toNanoOfDay()).substring(0, 6);
    return "ORC-" + data + "-" + sufixo;
  }
}
