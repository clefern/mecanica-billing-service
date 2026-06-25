package com.fiap.mecanica.billing.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand.ItemOrcamento;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrcamentoCalculoServiceTest {

  private OrcamentoCalculoService service;

  @BeforeEach
  void setUp() {
    service = new OrcamentoCalculoService();
    ReflectionTestUtils.setField(service, "taxaImpostos", new BigDecimal("0.05"));
  }

  @Test
  void calcular_mixDeItens_deveRetornarTotaisCorretos() {
    List<ItemOrcamento> itens = List.of(
        item("PECA", new BigDecimal("100.00"), 2),
        item("INSUMO", new BigDecimal("50.00"), 1),
        item("SERVICO", new BigDecimal("200.00"), 1)
    );

    OrcamentoCalculoService.ResultadoCalculo resultado = service.calcular(itens);

    assertThat(resultado.totalMateriais()).isEqualByComparingTo(new BigDecimal("250.00"));
    assertThat(resultado.totalMaoDeObra()).isEqualByComparingTo(new BigDecimal("200.00"));
    assertThat(resultado.valorImpostos()).isEqualByComparingTo(new BigDecimal("10.00"));
    assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("460.00"));
  }

  @Test
  void calcular_apenasPecas_deveZerarImpostoEMaoDeObra() {
    List<ItemOrcamento> itens = List.of(
        item("PECA", new BigDecimal("300.00"), 1)
    );

    OrcamentoCalculoService.ResultadoCalculo resultado = service.calcular(itens);

    assertThat(resultado.totalMateriais()).isEqualByComparingTo(new BigDecimal("300.00"));
    assertThat(resultado.totalMaoDeObra()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(resultado.valorImpostos()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
  }

  @Test
  void calcular_apenasServico_deveZerarMateriais() {
    List<ItemOrcamento> itens = List.of(
        item("SERVICO", new BigDecimal("400.00"), 1)
    );

    OrcamentoCalculoService.ResultadoCalculo resultado = service.calcular(itens);

    assertThat(resultado.totalMateriais()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(resultado.totalMaoDeObra()).isEqualByComparingTo(new BigDecimal("400.00"));
    assertThat(resultado.valorImpostos()).isEqualByComparingTo(new BigDecimal("20.00"));
    assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("420.00"));
  }

  @Test
  void calcular_listaVazia_deveRetornarTudoZero() {
    OrcamentoCalculoService.ResultadoCalculo resultado = service.calcular(List.of());

    assertThat(resultado.totalMateriais()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(resultado.totalMaoDeObra()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(resultado.valorImpostos()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(resultado.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void calcular_quantidadeMaiorQueUm_deveMultiplicarValorUnitario() {
    List<ItemOrcamento> itens = List.of(
        item("SERVICO", new BigDecimal("100.00"), 3)
    );

    OrcamentoCalculoService.ResultadoCalculo resultado = service.calcular(itens);

    assertThat(resultado.totalMaoDeObra()).isEqualByComparingTo(new BigDecimal("300.00"));
    assertThat(resultado.valorImpostos()).isEqualByComparingTo(new BigDecimal("15.00"));
    assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("315.00"));
  }

  private ItemOrcamento item(String tipo, BigDecimal valorUnitario, int quantidade) {
    return new ItemOrcamento(UUID.randomUUID(), tipo, "descricao-" + tipo, valorUnitario, quantidade);
  }
}
