package com.fiap.mecanica.billing.infra.security;

import com.fiap.mecanica.shared.kernel.security.ActionTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActionTokenConfig {

  @Bean
  public ActionTokenService actionTokenService(
      @Value("${mecanica.mail.action-token-secret}") String secret,
      @Value("${mecanica.mail.action-token-expiry-minutes}") int expiryMinutes) {
    return new ActionTokenService(secret, expiryMinutes);
  }
}
