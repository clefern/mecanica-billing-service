package com.fiap.mecanica.billing.infra.persistence.repository;

import com.fiap.mecanica.billing.infra.persistence.entity.ProcessedCommandEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedCommandJpaRepository extends JpaRepository<ProcessedCommandEntity, UUID> {

  boolean existsBySagaId(UUID sagaId);
}
