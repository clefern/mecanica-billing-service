# language: pt
Funcionalidade: Billing — ciclo de orçamento e pagamento
  Como sistema de billing da mecânica
  Quero processar orçamentos e pagamentos via Mercado Pago
  Para coordenar a etapa financeira da Saga

  Contexto:
    Dado que tenho um token de autenticação válido

  Cenário: Gerar orçamento ao receber comando da saga
    Quando a saga solicita geração de orçamento para uma nova OS
    Então o orçamento é criado com status "GERADO"
    E o evento OrcamentoCriado é publicado pelo billing

  Cenário: Comando duplicado é ignorado por idempotência
    Dado que um orçamento já foi gerado para uma saga
    Quando a saga solicita geração de orçamento novamente com o mesmo sagaId
    Então o evento OrcamentoCriado é publicado pelo billing exatamente 1 vez

  Cenário: Simular aprovação de pagamento
    Dado que existe um orçamento gerado para uma OS
    Quando simulo a aprovação do pagamento via endpoint
    Então recebo status HTTP 200
    E o evento PagamentoConfirmado é publicado pelo billing

  Cenário: Simular recusa de pagamento
    Dado que existe um orçamento gerado para uma OS
    Quando simulo a recusa do pagamento via endpoint
    Então recebo status HTTP 200
    E o evento PagamentoRecusado é publicado pelo billing

  Cenário: Listar orçamentos retorna lista autenticada
    Dado que existe um orçamento gerado para uma OS
    Quando listo os orçamentos via endpoint
    Então recebo status HTTP 200
    E a resposta contém pelo menos 1 orçamento
