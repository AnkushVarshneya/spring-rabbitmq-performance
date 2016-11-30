package com.genband.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private RabbitManagementTemplate rabbitManagementTemplate;

  @Bean
  public CommandLineRunner commandLineRunner() {
    return (args) -> {
      // durable and non-autodelete
      for (;;)
        // initTest();
        sendMessage();
      // muti thread and randomly send message via rk-x-x
    };
  }

  private void initTest() {
    sendMessage();
  }

  private void sendMessage() {
    rabbitTemplate.convertAndSend(exchange_name, "rk-1-1", "hellowolrd");
    rabbitTemplate.receive("q1");
  }
}
