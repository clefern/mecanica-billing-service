package com.fiap.mecanica.billing.infra.persistence.repository;

import com.fiap.mecanica.billing.infra.persistence.entity.PagamentoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoEntity, UUID> {

  Optional<PagamentoEntity> findByOrcamentoId(UUID orcamentoId);
}
