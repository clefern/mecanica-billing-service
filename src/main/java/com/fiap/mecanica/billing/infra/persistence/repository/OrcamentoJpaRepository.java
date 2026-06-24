package com.fiap.mecanica.billing.infra.persistence.repository;

import com.fiap.mecanica.billing.infra.persistence.entity.OrcamentoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoJpaRepository extends JpaRepository<OrcamentoEntity, UUID> {

  Optional<OrcamentoEntity> findBySagaId(UUID sagaId);
}
