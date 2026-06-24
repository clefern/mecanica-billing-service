package com.fiap.mecanica.billing.infra.persistence.mapper;

import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.domain.model.Pagamento;
import com.fiap.mecanica.billing.infra.persistence.entity.OrcamentoEntity;
import com.fiap.mecanica.billing.infra.persistence.entity.PagamentoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrcamentoMapper {

  OrcamentoEntity toEntity(Orcamento orcamento);

  Orcamento toDomain(OrcamentoEntity entity);

  PagamentoEntity toEntity(Pagamento pagamento);

  Pagamento toDomain(PagamentoEntity entity);
}
