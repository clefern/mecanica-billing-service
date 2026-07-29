package com.fiap.mecanica.billing.application.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrcamentoGeradoNotificacaoEvent(
    UUID osId,
    UUID orcamentoId,
    String orcamentoCodigo,
    LocalDateTime dataEmissao,
    LocalDateTime dataValidade,
    BigDecimal valorTotalMateriais,
    BigDecimal valorTotalMaoDeObra,
    BigDecimal valorImpostos,
    BigDecimal valorTotal,
    List<ItemOrcamentoNotificacao> itens) {

  public record ItemOrcamentoNotificacao(
      String descricao, String tipo, BigDecimal valorUnitario, int quantidade) {}
}
