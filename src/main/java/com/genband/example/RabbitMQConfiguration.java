package com.genband.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {
  public static final String QUEUE_NAME_MESSAGE = "queue.message";
  public static final String TOPIC_EXCHANGE_NAME = "exchange_test";
  public static final String ALTERNATIVE_EXCHANGE_NAME = "alternative_exchange";
  public static final String ALTERNATIVE_QUEUE_NAME = "alternative_queue";
  public static final String DIRECT_EXCHANGE_NAME = "direct_exchange_name";
  public static final String ROUNTING_KEY_1 = "key1";

  // public static final String IP = "172.28.250.5";
  public static final String IP = "localhost";

  private Logger logger = LoggerFactory.getLogger(RabbitMQConfiguration.class);

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(IP, 32158);
    connectionFactory.setUsername("guest");
    connectionFactory.setPassword("guest");
    return connectionFactory;
  }

  @Bean
  public RabbitManagementTemplate rabbitManagementTemplate() {
    RabbitManagementTemplate template =
        new RabbitManagementTemplate("http://guest:guest@" + IP + ":15672/api/");
    return template;
  }

  @Bean
  public AmqpAdmin amqpAdmin() {
    final RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
    return rabbitAdmin;
  }

  @Bean
  public RabbitTemplate rabbitTemplate() {
    RabbitTemplate template = new RabbitTemplate(connectionFactory());
    return template;
  }

  @Bean
  public Queue queueMessage() {
    return new Queue(QUEUE_NAME_MESSAGE, false, false, true);
  }

  @Bean
  public DirectExchange directExchange() {
    return new DirectExchange(DIRECT_EXCHANGE_NAME, false, true);
  }

  @Bean
  public Binding bindingExchangeMessage(Queue queueMessage, DirectExchange exchange) {
    return BindingBuilder.bind(queueMessage).to(exchange).with(ROUNTING_KEY_1);
  }

  @Bean
  public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory() {
    SimpleRabbitListenerContainerFactory container = new SimpleRabbitListenerContainerFactory();
    container.setConnectionFactory(connectionFactory());
    container.setConcurrentConsumers(5);
    return container;
  }
}
