package com.fiap.mecanica.billing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand;
import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand.ItemOrcamento;
import com.fiap.mecanica.billing.application.port.out.OrcamentoRepositoryPort;
import com.fiap.mecanica.billing.application.port.out.PagamentoRepositoryPort;
import com.fiap.mecanica.billing.domain.enums.StatusOrcamento;
import com.fiap.mecanica.billing.domain.enums.StatusPagamento;
import com.fiap.mecanica.billing.domain.exception.OrcamentoNaoEncontradoException;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.domain.model.Pagamento;
import com.fiap.mecanica.billing.domain.service.OrcamentoCalculoService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

  @Mock OrcamentoRepositoryPort orcamentoRepository;
  @Mock PagamentoRepositoryPort pagamentoRepository;
  @Mock OrcamentoCalculoService calculoService;

  @InjectMocks BillingService billingService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(billingService, "validadeDias", 30);
  }

  @Test
  void gerarOrcamento_deveCalcularEPersistirOrcamento() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    List<ItemOrcamento> itens = List.of(
        new ItemOrcamento(UUID.randomUUID(), "PECA", "filtro", new BigDecimal("100.00"), 1));
    GerarOrcamentoCommand command = new GerarOrcamentoCommand(sagaId, osId, itens);

    OrcamentoCalculoService.ResultadoCalculo calculo = new OrcamentoCalculoService.ResultadoCalculo(
        new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"));
    when(calculoService.calcular(itens)).thenReturn(calculo);
    when(orcamentoRepository.salvar(any())).thenAnswer(i -> i.getArgument(0));

    Orcamento resultado = billingService.gerarOrcamento(command);

    assertThat(resultado.getCodigo()).startsWith("ORC-");
    assertThat(resultado.getOsId()).isEqualTo(osId);
    assertThat(resultado.getSagaId()).isEqualTo(sagaId);
    assertThat(resultado.getValorTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(resultado.getStatus()).isEqualTo(StatusOrcamento.GERADO);
    verify(orcamentoRepository).salvar(any());
  }

  @Test
  void salvarPagamento_deveCriarPagamentoPendente() {
    UUID orcId = UUID.randomUUID();
    when(pagamentoRepository.salvar(any())).thenAnswer(i -> i.getArgument(0));

    Pagamento resultado = billingService.salvarPagamento(orcId, "pref-123", "http://mp/pay");

    ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
    verify(pagamentoRepository).salvar(captor.capture());
    assertThat(captor.getValue().getOrcamentoId()).isEqualTo(orcId);
    assertThat(captor.getValue().getStatus()).isEqualTo(StatusPagamento.PENDENTE);
    assertThat(captor.getValue().getMpPreferenceId()).isEqualTo("pref-123");
  }

  @Test
  void aprovarPagamento_deveAtualizarStatusParaAprovado() {
    UUID orcId = UUID.randomUUID();
    Pagamento pagamento = Pagamento.builder()
        .id(UUID.randomUUID())
        .orcamentoId(orcId)
        .status(StatusPagamento.PENDENTE)
        .build();
    when(pagamentoRepository.buscarPorOrcamentoId(orcId)).thenReturn(Optional.of(pagamento));
    when(pagamentoRepository.salvar(any())).thenAnswer(i -> i.getArgument(0));

    Pagamento resultado = billingService.aprovarPagamento(orcId, "mp-pay-456");

    assertThat(resultado.getStatus()).isEqualTo(StatusPagamento.APROVADO);
    assertThat(resultado.getMpPaymentId()).isEqualTo("mp-pay-456");
  }

  @Test
  void recusarPagamento_deveAtualizarStatusParaRecusado() {
    UUID orcId = UUID.randomUUID();
    Pagamento pagamento = Pagamento.builder()
        .id(UUID.randomUUID())
        .orcamentoId(orcId)
        .status(StatusPagamento.PENDENTE)
        .build();
    when(pagamentoRepository.buscarPorOrcamentoId(orcId)).thenReturn(Optional.of(pagamento));
    when(pagamentoRepository.salvar(any())).thenAnswer(i -> i.getArgument(0));

    Pagamento resultado = billingService.recusarPagamento(orcId, "saldo insuficiente");

    assertThat(resultado.getStatus()).isEqualTo(StatusPagamento.RECUSADO);
  }

  @Test
  void buscarPorId_orcamentoNaoEncontrado_deveLancarException() {
    UUID id = UUID.randomUUID();
    when(orcamentoRepository.buscarPorId(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> billingService.buscarPorId(id))
        .isInstanceOf(OrcamentoNaoEncontradoException.class);
  }
}
