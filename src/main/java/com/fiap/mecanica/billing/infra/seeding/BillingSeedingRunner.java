package com.fiap.mecanica.billing.infra.seeding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BillingSeedingRunner implements CommandLineRunner {

  @Value("${seeding.enabled:true}")
  private boolean seedingEnabled;

  @Override
  public void run(String... args) {
    if (!seedingEnabled) {
      log.info("Seeding desabilitado.");
      return;
    }
    log.info("[SEED] billing-service inicializado — sem dados de seed necessários.");
  }
}
