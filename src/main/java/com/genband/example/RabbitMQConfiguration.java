package com.genband.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.CacheMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RabbitMQConfiguration {
  // 172.28.250.5;
  // @Value("${ip:localhost}")
  // private String IP;
  @Value("${ip:172.28.250.5}")
  private String IP;

  // 32158
  // @Value("${rabbitmq.port:5672}")
  // private int RABBITMQ_PORT;
  @Value("${rabbitmq.port:32158}")
  private int RABBITMQ_PORT;

  // 32160
  // @Value("${rabbitmq.management.port: 15672}")
  // private int RABBITMQ_MANAGEMENT_PORT;
  @Value("${rabbitmq.management.port: 32160}")
  private int RABBITMQ_MANAGEMENT_PORT;

  private Logger logger = LoggerFactory.getLogger(RabbitMQConfiguration.class);

  @Bean
  public ConnectionFactory connectionFactory() {
    // CachingConnectionFactory connectionFactory = new CachingConnectionFactory(IP, 32158);
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(IP, RABBITMQ_PORT);
    connectionFactory.setUsername("guest");
    connectionFactory.setPassword("guest");
    connectionFactory.setConnectionCacheSize(120);
    connectionFactory.setCacheMode(CacheMode.CONNECTION);
    return connectionFactory;
  }

  @Bean
  public RabbitManagementTemplate rabbitManagementTemplate() {
    RabbitManagementTemplate template = new RabbitManagementTemplate(
        "http://guest:guest@" + IP + ":" + RABBITMQ_MANAGEMENT_PORT + "/api/");
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
  public RabbitMessagingTemplate rabbitMessagingTemplate() {
    RabbitMessagingTemplate template = new RabbitMessagingTemplate(rabbitTemplate());
    return template;
  }

  // combined with @Component and @RabbitListener
  @Bean
  public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory() {
    SimpleRabbitListenerContainerFactory container = new SimpleRabbitListenerContainerFactory();
    container.setConnectionFactory(connectionFactory());
    container.setConcurrentConsumers(30);
    return container;
  }

  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
    pool.setCorePoolSize(100);
    pool.setMaxPoolSize(200);
    pool.setWaitForTasksToCompleteOnShutdown(true);
    return pool;
  }

  @Bean
  public ThreadPoolTaskExecutor sendMessageTaskExecutor() {
    ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
    pool.setCorePoolSize(10);
    pool.setMaxPoolSize(20);
    pool.setWaitForTasksToCompleteOnShutdown(true);
    return pool;
  }


  @Bean
  public SimpleMessageListenerContainer listenerContainer() {
    SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
    listenerContainer.setConnectionFactory(connectionFactory());

    for (int i = 1; i <= 50; i++) {
      listenerContainer.addQueueNames("q" + i);
    }

    listenerContainer.setMessageListener(new Consumer());
    listenerContainer.setConcurrentConsumers(100);
    listenerContainer.setAcknowledgeMode(AcknowledgeMode.NONE);
    return listenerContainer;
  }

  public class Consumer implements MessageListener {

    @Override
    public void onMessage(Message message) {
      // System.out.println(new String(message.getBody()));
    }
  }
}
