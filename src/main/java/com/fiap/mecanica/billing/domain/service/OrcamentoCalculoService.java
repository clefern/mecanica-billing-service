package com.fiap.mecanica.billing.domain.service;

import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrcamentoCalculoService {

  @Value("${mecanica.orcamento.taxa-impostos:0.05}")
  private BigDecimal taxaImpostos;

  public ResultadoCalculo calcular(List<GerarOrcamentoCommand.ItemOrcamento> itens) {
    BigDecimal totalMateriais = itens.stream()
        .filter(i -> "PECA".equals(i.tipo()) || "INSUMO".equals(i.tipo()))
        .map(i -> i.valorUnitario().multiply(BigDecimal.valueOf(i.quantidade())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalMaoDeObra = itens.stream()
        .filter(i -> "SERVICO".equals(i.tipo()))
        .map(i -> i.valorUnitario().multiply(BigDecimal.valueOf(i.quantidade())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal valorImpostos = totalMaoDeObra.multiply(taxaImpostos)
        .setScale(2, RoundingMode.HALF_UP);

    BigDecimal valorTotal = totalMateriais.add(totalMaoDeObra).add(valorImpostos);

    return new ResultadoCalculo(totalMateriais, totalMaoDeObra, valorImpostos, valorTotal);
  }

  public record ResultadoCalculo(
      BigDecimal totalMateriais,
      BigDecimal totalMaoDeObra,
      BigDecimal valorImpostos,
      BigDecimal valorTotal) {
  }
}
