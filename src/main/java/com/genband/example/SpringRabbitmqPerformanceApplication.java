package com.genband.example;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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

  @Value("${number.of.queues: 100}")
  private int number_of_queues;

  // number of routing keys on each queue
  @Value("${number.of.routing.keys: 100}")
  private int number_of_routing_keys;

  @Value("${queue.prefix:q}")
  private String queue_prefix;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private RabbitManagementTemplate rabbitManagementTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  // http://docs.spring.io/spring-amqp/docs/1.4.5.RELEASE/reference/html/amqp.html
  // 3.2 Connection and Resource Management
  @Bean
  public CommandLineRunner commandLineRunner() {
    return (args) -> {
      // durable and non-autodelete
      // initTest();
      System.out.println("number of queues: " + number_of_queues);
      System.out.println("number of routing keys on each queue: " + number_of_routing_keys);
      // createExchange();
      // cleanQueues();
      // createQueues();
      // bindQueueToExchangeWithRoutingKey();
      for (;;) {
        // create exchange if it not existed
        Thread.sleep(100);
        sendMessageWithRandomRoutingKey();
      }

      // sendMessage();
      // TODO muti thread and randomly send message via rk-x-x
    };
  }

  private void cleanQueues() {
    List<Queue> queues = rabbitManagementTemplate.getQueues("/");
    for (Queue queue : queues) {
      rabbitManagementTemplate.deleteQueue(queue);
    }
  }

  // rabbitmq management doesn't support
  private void bindQueueToExchangeWithRoutingKey() {
    for (int i = 1; i <= number_of_queues; i++) {
      for (int j = 1; j <= number_of_routing_keys; j++) {
        amqpAdmin.declareBinding(new Binding("q" + i, DestinationType.QUEUE, exchange_name,
            "rk-" + i + "-" + j, new HashMap<String, Object>()));
        System.out.println("bind q" + i + " to " + exchange_name + " with " + "rk-" + i + "-" + j);
      }
    }

  }

  private void createQueues() {
    for (int i = 1; i <= number_of_queues; i++) {
      System.out.println("Create queue:" + queue_prefix + i);
      amqpAdmin.declareQueue(new Queue(queue_prefix + i, true));
    }
  }

  // no harm if it's same
  private void createExchange() {
    rabbitManagementTemplate.addExchange(new DirectExchange(exchange_name, true, false));
  }

  private void sendMessageWithRandomRoutingKey() {
    int random_queue_number = new Random().nextInt(number_of_queues) + 1;
    int random_routingKey_number = new Random().nextInt(number_of_routing_keys) + 1;
    String routingKey = "rk-" + random_queue_number + "-" + random_routingKey_number;
    System.out.println(routingKey);
    rabbitTemplate.convertAndSend(exchange_name, routingKey, "helloworld");
  }

  // TODO multiple thread binding routing-key
  // TODO clean all queues
}
