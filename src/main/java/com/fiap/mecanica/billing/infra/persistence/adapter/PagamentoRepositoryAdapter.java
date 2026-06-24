package com.fiap.mecanica.billing.infra.persistence.adapter;

import com.fiap.mecanica.billing.application.port.out.PagamentoRepositoryPort;
import com.fiap.mecanica.billing.domain.model.Pagamento;
import com.fiap.mecanica.billing.infra.persistence.mapper.OrcamentoMapper;
import com.fiap.mecanica.billing.infra.persistence.repository.PagamentoJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PagamentoRepositoryAdapter implements PagamentoRepositoryPort {

  private final PagamentoJpaRepository jpaRepository;
  private final OrcamentoMapper mapper;

  @Override
  public Pagamento salvar(Pagamento pagamento) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(pagamento)));
  }

  @Override
  public Optional<Pagamento> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Pagamento> buscarPorOrcamentoId(UUID orcamentoId) {
    return jpaRepository.findByOrcamentoId(orcamentoId).map(mapper::toDomain);
  }
}
