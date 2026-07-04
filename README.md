# mecanica-billing-service

> Responsável pela geração de orçamentos e processamento de pagamentos via Mercado Pago na Fase 4 (Grupo 14SOAT).

## Responsabilidade na Saga

Este serviço atua como **participante da Saga** no passo financeiro. Ao receber `GerarOrcamentoCommand`, calcula o valor total dos itens (peças, insumos e mão de obra com impostos), cria uma preferência de pagamento no Mercado Pago e publica o resultado para o orquestrador (`os-service`). A confirmação ou recusa de pagamento (via webhook ou simulação) determina se a Saga avança ou compensa.

```
os-service → [GerarOrcamentoCommand] → billing-service
billing-service → Mercado Pago → [OrcamentoCriadoEvent] → os-service
webhook/simular → [PagamentoConfirmadoEvent | PagamentoRecusadoEvent] → os-service
```

## Endpoints REST

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `/api/billing/orcamentos` | Listar orçamentos (paginado) |
| `GET` | `/api/billing/orcamentos/{id}` | Buscar orçamento por ID |
| `POST` | `/api/billing/webhooks/mercadopago` | Webhook oficial do Mercado Pago |
| `POST` | `/api/billing/webhooks/simular` | Simular aprovação ou recusa de pagamento |

Swagger: `http://localhost:8081/swagger-ui.html`

## Mensagens RabbitMQ

### Consome
| Queue | Tipo | Ação |
|-------|------|------|
| `mecanica.billing.gerar-orcamento` | `GerarOrcamentoCommand` | Cria orçamento + preferência MP |

### Publica
| Routing Key | Tipo | Condição |
|-------------|------|----------|
| `os.orcamento-criado` | `OrcamentoCriadoEvent` | Orçamento gerado com sucesso |
| `os.falha-no-billing` | `FalhaNoBillingEvent` | Erro ao criar preferência MP |
| `os.pagamento-confirmado` | `PagamentoConfirmadoEvent` | Pagamento aprovado |
| `os.pagamento-recusado` | `PagamentoRecusadoEvent` | Pagamento recusado |

Idempotência garantida por `processed_commands` (deduplicação por `sagaId`).

## Como rodar localmente

```bash
# Stack completa (todos os MS + infra)
cd ms-infra-ms/mecanica-fiap
docker compose -f docker-compose.full.yml up --build

# Simular pagamento aprovado (substitua {orcamentoId})
curl -s -X POST http://localhost/api/billing/webhooks/simular \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"orcamentoId":"{orcamentoId}","decisao":"APROVADO"}'
```

## Testes

```bash
./mvnw test                       # unitários + BDD
./mvnw test -Dtest="CucumberTest" # apenas BDD
```

O BDD usa **Testcontainers** (PostgreSQL real) + `@MockBean` no `MercadoPagoGateway` e `BillingEventPublisher` — requer Docker em execução.

## Tech stack

| | |
|-|-|
| **Java** | 21 |
| **Framework** | Spring Boot 3.5.x |
| **Banco** | PostgreSQL 16 (porta 5433) |
| **Mensageria** | RabbitMQ 3.13 |
| **Pagamentos** | Mercado Pago SDK 2.1.26 |
| **Migrations** | Flyway |
| **Segurança** | JWT (JJWT 0.12) |
| **Porta** | 8081 |
| **Cobertura** | JaCoCo ≥ 80% |
| **BDD** | Cucumber 7.21 + JUnit Platform Suite |
