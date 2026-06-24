package com.fiap.mecanica.billing.application.port.out;

import com.fiap.mecanica.billing.domain.model.Pagamento;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepositoryPort {

  Pagamento salvar(Pagamento pagamento);

  Optional<Pagamento> buscarPorId(UUID id);

  Optional<Pagamento> buscarPorOrcamentoId(UUID orcamentoId);
}
