package com.fiap.mecanica.billing.presentation.controller;

import com.fiap.mecanica.billing.application.service.BillingService;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.presentation.dto.OrcamentoResponse;
import com.fiap.mecanica.shared.kernel.security.ActionTokenPayload;
import com.fiap.mecanica.shared.kernel.security.ActionTokenService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Endpoint público (sem login) clicado pelo cliente no link de aprovação/recusa de orçamento
 * enviado por email — o token HMAC garante que só quem recebeu o email pode decidir.
 */
@RestController
@RequestMapping("/api/billing/integracoes/orcamentos")
@RequiredArgsConstructor
public class IntegracaoOrcamentoController {

  private final BillingService billingService;
  private final ActionTokenService actionTokenService;

  @GetMapping("/aprovacao")
  public ResponseEntity<OrcamentoResponse> processarAprovacaoPorToken(@RequestParam String token) {
    Optional<ActionTokenPayload> payload = actionTokenService.validate(token);
    if (payload.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido ou expirado");
    }

    Orcamento orcamento =
        switch (payload.get().decisao()) {
          case APROVADO -> billingService.aprovar(payload.get().orcamentoId());
          case REPROVADO -> billingService.reprovar(payload.get().orcamentoId());
        };

    return ResponseEntity.ok(toResponse(orcamento));
  }

  private OrcamentoResponse toResponse(Orcamento orc) {
    return new OrcamentoResponse(
        orc.getId(), orc.getCodigo(), orc.getOsId(), orc.getSagaId(),
        orc.getDataEmissao(), orc.getDataValidade(),
        orc.getValorTotalMateriais(), orc.getValorTotalMaoDeObra(),
        orc.getValorImpostos(), orc.getValorTotal(), orc.getStatus());
  }
}
