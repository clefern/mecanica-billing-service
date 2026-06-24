package com.fiap.mecanica.billing.domain.exception;

public class PagamentoException extends RuntimeException {

  public PagamentoException(String message) {
    super(message);
  }

  public PagamentoException(String message, Throwable cause) {
    super(message, cause);
  }
}
