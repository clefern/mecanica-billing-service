package com.fiap.mecanica.billing.infra.persistence.adapter;

import com.fiap.mecanica.billing.application.port.out.OrcamentoRepositoryPort;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.infra.persistence.mapper.OrcamentoMapper;
import com.fiap.mecanica.billing.infra.persistence.repository.OrcamentoJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrcamentoRepositoryAdapter implements OrcamentoRepositoryPort {

  private final OrcamentoJpaRepository jpaRepository;
  private final OrcamentoMapper mapper;

  @Override
  public Orcamento salvar(Orcamento orcamento) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(orcamento)));
  }

  @Override
  public Optional<Orcamento> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Orcamento> buscarPorSagaId(UUID sagaId) {
    return jpaRepository.findBySagaId(sagaId).map(mapper::toDomain);
  }

  @Override
  public Page<Orcamento> listarTodas(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }
}
