package com.fiap.mecanica.billing.presentation.controller;

import com.fiap.mecanica.billing.application.service.BillingService;
import com.fiap.mecanica.billing.domain.model.Orcamento;
import com.fiap.mecanica.billing.presentation.dto.OrcamentoResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

  private final BillingService billingService;

  @GetMapping
  public ResponseEntity<Page<OrcamentoResponse>> listar(Pageable pageable) {
    return ResponseEntity.ok(billingService.listarTodas(pageable).map(this::toResponse));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrcamentoResponse> buscar(@PathVariable UUID id) {
    return ResponseEntity.ok(toResponse(billingService.buscarPorId(id)));
  }

  private OrcamentoResponse toResponse(Orcamento orc) {
    return new OrcamentoResponse(
        orc.getId(), orc.getCodigo(), orc.getOsId(), orc.getSagaId(),
        orc.getDataEmissao(), orc.getDataValidade(),
        orc.getValorTotalMateriais(), orc.getValorTotalMaoDeObra(),
        orc.getValorImpostos(), orc.getValorTotal(), orc.getStatus());
  }
}
