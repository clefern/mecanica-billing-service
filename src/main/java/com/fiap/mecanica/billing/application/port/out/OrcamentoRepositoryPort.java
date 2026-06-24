package com.fiap.mecanica.billing.application.port.out;

import com.fiap.mecanica.billing.domain.model.Orcamento;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrcamentoRepositoryPort {

  Orcamento salvar(Orcamento orcamento);

  Optional<Orcamento> buscarPorId(UUID id);

  Optional<Orcamento> buscarPorSagaId(UUID sagaId);

  Page<Orcamento> listarTodas(Pageable pageable);
}
