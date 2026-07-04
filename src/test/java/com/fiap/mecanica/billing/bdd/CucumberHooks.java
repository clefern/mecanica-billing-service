package com.fiap.mecanica.billing.bdd;

import com.fiap.mecanica.billing.infra.persistence.repository.OrcamentoJpaRepository;
import com.fiap.mecanica.billing.infra.persistence.repository.PagamentoJpaRepository;
import com.fiap.mecanica.billing.infra.persistence.repository.ProcessedCommandJpaRepository;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CucumberHooks {

  private final PagamentoJpaRepository pagamentoRepo;
  private final OrcamentoJpaRepository orcamentoRepo;
  private final ProcessedCommandJpaRepository processedRepo;

  @Before
  public void limparBanco() {
    pagamentoRepo.deleteAll();
    orcamentoRepo.deleteAll();
    processedRepo.deleteAll();
  }
}
