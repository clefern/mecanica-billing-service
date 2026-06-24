package com.fiap.mecanica.billing.infra.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  public static final String EXCHANGE = "mecanica.direct";

  public static final String QUEUE_GERAR_ORCAMENTO = "mecanica.billing.gerar-orcamento";
  public static final String QUEUE_ORCAMENTO_CRIADO = "mecanica.os.orcamento-criado";
  public static final String QUEUE_FALHA_BILLING = "mecanica.os.falha-no-billing";
  public static final String QUEUE_PAGAMENTO_CONFIRMADO = "mecanica.os.pagamento-confirmado";
  public static final String QUEUE_PAGAMENTO_RECUSADO = "mecanica.os.pagamento-recusado";

  public static final String RK_GERAR_ORCAMENTO = "billing.gerar-orcamento";
  public static final String RK_ORCAMENTO_CRIADO = "os.orcamento-criado";
  public static final String RK_FALHA_BILLING = "os.falha-no-billing";
  public static final String RK_PAGAMENTO_CONFIRMADO = "os.pagamento-confirmado";
  public static final String RK_PAGAMENTO_RECUSADO = "os.pagamento-recusado";

  @Bean
  DirectExchange mecanicaExchange() {
    return new DirectExchange(EXCHANGE, true, false);
  }

  @Bean
  Queue filaGerarOrcamento() {
    return QueueBuilder.durable(QUEUE_GERAR_ORCAMENTO).build();
  }

  @Bean
  Queue filaOrcamentoCriado() {
    return QueueBuilder.durable(QUEUE_ORCAMENTO_CRIADO).build();
  }

  @Bean
  Queue filaFalhaBilling() {
    return QueueBuilder.durable(QUEUE_FALHA_BILLING).build();
  }

  @Bean
  Queue filaPagamentoConfirmado() {
    return QueueBuilder.durable(QUEUE_PAGAMENTO_CONFIRMADO).build();
  }

  @Bean
  Queue filaPagamentoRecusado() {
    return QueueBuilder.durable(QUEUE_PAGAMENTO_RECUSADO).build();
  }

  @Bean
  Binding bindingGerarOrcamento(Queue filaGerarOrcamento, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaGerarOrcamento).to(mecanicaExchange).with(RK_GERAR_ORCAMENTO);
  }

  @Bean
  Binding bindingOrcamentoCriado(Queue filaOrcamentoCriado, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaOrcamentoCriado).to(mecanicaExchange).with(RK_ORCAMENTO_CRIADO);
  }

  @Bean
  Binding bindingFalhaBilling(Queue filaFalhaBilling, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaFalhaBilling).to(mecanicaExchange).with(RK_FALHA_BILLING);
  }

  @Bean
  Binding bindingPagamentoConfirmado(Queue filaPagamentoConfirmado, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaPagamentoConfirmado).to(mecanicaExchange).with(RK_PAGAMENTO_CONFIRMADO);
  }

  @Bean
  Binding bindingPagamentoRecusado(Queue filaPagamentoRecusado, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaPagamentoRecusado).to(mecanicaExchange).with(RK_PAGAMENTO_RECUSADO);
  }

  @Bean
  Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
      Jackson2JsonMessageConverter converter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(converter);
    return template;
  }
}
