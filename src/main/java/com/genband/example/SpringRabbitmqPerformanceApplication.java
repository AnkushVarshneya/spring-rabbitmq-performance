package com.genband.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringRabbitmqPerformanceApplication {

  private Logger logger = LoggerFactory.getLogger(SpringRabbitmqPerformanceApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(SpringRabbitmqPerformanceApplication.class, args);
  }

  @Value("${exchange.name:e1}")
  private String exchange_name;
  
  @Value("${queue}")

  @Autowired
  private RabbitManagementTemplate rabbitManagementTemplate;

  @Bean
  public CommandLineRunner commandLineRunner() {
    return (args) -> {
      // durable and non-autodelete
      initTest();
    };
  }
  // remove exchange

  private void initTest() {
    addExchange();
    addQueues();
  }

  private void addQueues() {
    
  }

  private void addExchange() {
    Exchange exchange = rabbitManagementTemplate.getExchange(exchange_name);
    if (exchange != null) {
      logger.info("found duplicated exchange which will be removed");
      rabbitManagementTemplate.deleteExchange(exchange);
    }
    rabbitManagementTemplate.addExchange(new DirectExchange(exchange_name));
    logger.info("add new exchange:" + exchange_name);
  }

  // create exchange
  // remove queues
  // create queues

}
