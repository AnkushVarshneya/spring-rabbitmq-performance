package com.genband.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.CacheMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RabbitMQConfiguration {
  public static final String IP = "172.28.250.5";
  // public static final String IP = "localhost";

  private Logger logger = LoggerFactory.getLogger(RabbitMQConfiguration.class);

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory(IP, 32158);
    // CachingConnectionFactory connectionFactory = new CachingConnectionFactory(IP);
    connectionFactory.setUsername("guest");
    connectionFactory.setPassword("guest");
    connectionFactory.setConnectionCacheSize(120);
    connectionFactory.setCacheMode(CacheMode.CONNECTION);
    return connectionFactory;
  }

  @Bean
  public RabbitManagementTemplate rabbitManagementTemplate() {
    RabbitManagementTemplate template =
        new RabbitManagementTemplate("http://guest:guest@" + IP + ":32160/api/");
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

  @Bean
  public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory() {
    SimpleRabbitListenerContainerFactory container = new SimpleRabbitListenerContainerFactory();
    container.setConnectionFactory(connectionFactory());
    container.setConcurrentConsumers(5);
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
}
