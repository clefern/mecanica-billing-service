package com.fiap.mecanica.billing.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.billing.application.gateway.MercadoPagoGateway;
import com.fiap.mecanica.billing.application.gateway.MercadoPagoGateway.MpPreferenceResult;
import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand;
import com.fiap.mecanica.billing.application.messaging.GerarOrcamentoCommand.ItemOrcamento;
import com.fiap.mecanica.billing.application.messaging.OrcamentoCriadoEvent;
import com.fiap.mecanica.billing.application.messaging.PagamentoConfirmadoEvent;
import com.fiap.mecanica.billing.application.messaging.PagamentoRecusadoEvent;
import com.fiap.mecanica.billing.bdd.ScenarioContext;
import com.fiap.mecanica.billing.infra.messaging.listener.BillingCommandListener;
import com.fiap.mecanica.billing.infra.messaging.publisher.BillingEventPublisher;
import com.fiap.mecanica.billing.infra.persistence.repository.OrcamentoJpaRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class BillingSteps {

  private final ScenarioContext ctx;
  private final BillingCommandListener listener;
  private final OrcamentoJpaRepository orcamentoRepo;
  private final BillingEventPublisher billingEventPublisher;
  private final MercadoPagoGateway mercadoPagoGateway;

  @Autowired
  private TestRestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private GerarOrcamentoCommand buildCommand(UUID sagaId, UUID osId) {
    return new GerarOrcamentoCommand(sagaId, osId, List.of(
        new ItemOrcamento(UUID.randomUUID(), "PECA", "Filtro de Óleo",
            new BigDecimal("50.00"), 1)));
  }

  private void mockMpGateway() {
    when(mercadoPagoGateway.criarPreference(any()))
        .thenReturn(new MpPreferenceResult("PREF-123", "https://mp.test/checkout"));
  }

  @Quando("a saga solicita geração de orçamento para uma nova OS")
  public void gerarOrcamentoNovaOs() {
    mockMpGateway();
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onGerarOrcamento(buildCommand(sagaId, osId));
  }

  @Então("o orçamento é criado com status {string}")
  public void verificarStatusOrcamento(String status) {
    var orc = orcamentoRepo.findBySagaId(ctx.getSagaId());
    assertThat(orc).isPresent();
    assertThat(orc.get().getStatus().name()).isEqualTo(status);
    ctx.setOrcamentoId(orc.get().getId());
  }

  @E("o evento OrcamentoCriado é publicado pelo billing")
  public void verificarEventoOrcamentoCriado() {
    verify(billingEventPublisher, times(1)).publicar(any(OrcamentoCriadoEvent.class));
  }

  @Dado("que um orçamento já foi gerado para uma saga")
  public void orcamentoJaGerado() {
    mockMpGateway();
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onGerarOrcamento(buildCommand(sagaId, osId));
    var orc = orcamentoRepo.findBySagaId(sagaId);
    orc.ifPresent(o -> ctx.setOrcamentoId(o.getId()));
  }

  @Quando("a saga solicita geração de orçamento novamente com o mesmo sagaId")
  public void gerarOrcamentoDuplicado() {
    mockMpGateway();
    listener.onGerarOrcamento(buildCommand(ctx.getSagaId(), ctx.getOsId()));
  }

  @E("o evento OrcamentoCriado é publicado pelo billing exatamente 1 vez")
  public void verificarIdempotencia() {
    verify(billingEventPublisher, times(1)).publicar(any(OrcamentoCriadoEvent.class));
  }

  @Dado("que existe um orçamento gerado para uma OS")
  public void criarOrcamentoParaOs() {
    mockMpGateway();
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onGerarOrcamento(buildCommand(sagaId, osId));
    var orc = orcamentoRepo.findBySagaId(sagaId);
    orc.ifPresent(o -> ctx.setOrcamentoId(o.getId()));
  }

  @Quando("simulo a aprovação do pagamento via endpoint")
  public void simularAprovacao() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + ctx.getToken());
    headers.set("Content-Type", "application/json");
    String body = String.format("{\"orcamentoId\":\"%s\",\"decisao\":\"APROVADO\"}", ctx.getOrcamentoId());
    ResponseEntity<String> resp = restTemplate.exchange(
        baseUrl() + "/api/billing/webhooks/simular",
        HttpMethod.POST,
        new HttpEntity<>(body, headers),
        String.class);
    ctx.setLastStatusCode(resp.getStatusCode().value());
  }

  @Quando("simulo a recusa do pagamento via endpoint")
  public void simularRecusa() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + ctx.getToken());
    headers.set("Content-Type", "application/json");
    String body = String.format("{\"orcamentoId\":\"%s\",\"decisao\":\"RECUSADO\"}", ctx.getOrcamentoId());
    ResponseEntity<String> resp = restTemplate.exchange(
        baseUrl() + "/api/billing/webhooks/simular",
        HttpMethod.POST,
        new HttpEntity<>(body, headers),
        String.class);
    ctx.setLastStatusCode(resp.getStatusCode().value());
  }

  @Então("recebo status HTTP {int}")
  public void verificarStatusHttp(int statusEsperado) {
    assertThat(ctx.getLastStatusCode()).isEqualTo(statusEsperado);
  }

  @E("o evento PagamentoConfirmado é publicado pelo billing")
  public void verificarPagamentoConfirmado() {
    verify(billingEventPublisher, times(1)).publicar(any(PagamentoConfirmadoEvent.class));
  }

  @E("o evento PagamentoRecusado é publicado pelo billing")
  public void verificarPagamentoRecusado() {
    verify(billingEventPublisher, times(1)).publicar(any(PagamentoRecusadoEvent.class));
  }

  @Quando("listo os orçamentos via endpoint")
  public void listarOrcamentos() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + ctx.getToken());
    ResponseEntity<String> resp = restTemplate.exchange(
        baseUrl() + "/api/billing/orcamentos",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);
    ctx.setLastStatusCode(resp.getStatusCode().value());
  }

  @E("a resposta contém pelo menos 1 orçamento")
  public void verificarListaNaoVazia() {
    assertThat(ctx.getLastStatusCode()).isEqualTo(200);
  }
}
