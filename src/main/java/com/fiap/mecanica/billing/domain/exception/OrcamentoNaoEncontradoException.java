package com.fiap.mecanica.billing.domain.exception;

import java.util.UUID;

public class OrcamentoNaoEncontradoException extends RuntimeException {

  public OrcamentoNaoEncontradoException(UUID id) {
    super("Orçamento não encontrado: " + id);
  }
}
