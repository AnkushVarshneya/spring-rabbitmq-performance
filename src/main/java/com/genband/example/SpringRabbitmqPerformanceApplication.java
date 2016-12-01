package com.genband.example;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.NumberOfDocuments;
import javax.xml.bind.annotation.W3CDomHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
      createExchange();
      cleanQueues();
      createQueues();
      bindQueueToExchangeWithRoutingKey(1, 1, 10);

      // sendMessage();
    };
  }

  private void cleanQueues() {
    List<Queue> queues = rabbitManagementTemplate.getQueues("/");
    for (Queue queue : queues) {
      rabbitManagementTemplate.deleteQueue(queue);
    }
  }

  private void bindQueueToExchangeWithRoutingKey(int interval, int startRoutingKeyIndex,
      int endRoutingKeyIndex) {
    int thread_counter = 1;
    for (int i = 1; i <= number_of_queues; i = i + interval) {
      int startQueueIndex = i;
      int endQueueIndex = i + interval - 1;
      if (endQueueIndex + interval - 1 > number_of_queues) {
        endQueueIndex = number_of_queues;
      }
      System.out.println("start thread:" + (thread_counter++) + " queue index from "
          + startQueueIndex + " to  " + endQueueIndex);
      Thread thread = new Thread(new BindQueueToExchangeWithRoutingKey(startQueueIndex,
          endQueueIndex, startRoutingKeyIndex, endRoutingKeyIndex));
      thread.start();
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

  @Component
  @Scope("prototype")
  public class BindQueueToExchangeWithRoutingKey extends Thread {
    private int startQueueIndex = 0;
    private int endQueueIndex = -1;
    private int startRoutingKeyIndex = 0;
    private int endRoutingKeyIndex = -1;

    public BindQueueToExchangeWithRoutingKey(int startQueueIndex, int endQueueIndex,
        int startRoutingKeyindex, int endRoutingKeyIndex) {
      this.startQueueIndex = startQueueIndex;
      this.endQueueIndex = endQueueIndex;
      this.startRoutingKeyIndex = startRoutingKeyindex;
      this.endRoutingKeyIndex = endRoutingKeyIndex;
    }

    @Override
    public void run() {
      for (int i = startQueueIndex; i <= endQueueIndex; i++) {
        for (int j = startRoutingKeyIndex; j <= endRoutingKeyIndex; j++) {
          amqpAdmin.declareBinding(new Binding("q" + i, DestinationType.QUEUE, exchange_name,
              "rk-" + i + "-" + j, new HashMap<String, Object>()));
          System.out
              .println("bind q" + i + " to " + exchange_name + " with " + "rk-" + i + "-" + j);
        }
      }
    }

  }
  // TODO clean all queues
}
