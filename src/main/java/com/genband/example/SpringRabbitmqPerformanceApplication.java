package com.genband.example;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class SpringRabbitmqPerformanceApplication {

  private Logger logger = LoggerFactory.getLogger(SpringRabbitmqPerformanceApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(SpringRabbitmqPerformanceApplication.class, args);
  }

  @Value("${exchange.name:e1}")
  public String EXCHANGE_NAME;

  @Value("${number.of.queues:50}")
  public int NUMBER_OF_QUEUES;

  // number of routing keys on each queue
  @Value("${number.of.routing.keys:100}")
  public int NUMBER_OF_ROUTING_KEYS;

  @Value("${queue.prefix:q}")
  public String QUEUE_PREFIX;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private RabbitManagementTemplate rabbitManagementTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Autowired
  private ThreadPoolTaskExecutor taskExecutor;

  // http://docs.spring.io/spring-amqp/docs/1.4.5.RELEASE/reference/html/amqp.html
  // 3.2 Connection and Resource Management
  @Bean
  public CommandLineRunner commandLineRunner() {
    return (args) -> {
      // durable and non-autodelete
      // initTest();
      System.out.println("number of queues: " + NUMBER_OF_QUEUES);
      System.out.println("number of routing keys on each queue: " + NUMBER_OF_ROUTING_KEYS);

      // createExchange_createQueue_BindQueue();
      sendMessage();
    };
  }

  private void sendMessage() {
    for (;;) {
      // Thread.sleep(100);
      sendMessageWithRandomRoutingKey();
    }
  }

  private void createExchange_createQueue_BindQueue() {
    createExchange();
    cleanQueues();
    createQueues();
    bindQueueToExchangeWithRoutingKey(1, 1, NUMBER_OF_ROUTING_KEYS);
    for (;;) {
      int count = taskExecutor.getActiveCount();
      System.out.println("Active Threads : " + count);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (count == 0) {
        taskExecutor.shutdown();
        System.out.println("binding finished");
        break;
      }
    }
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
    for (int i = 1; i <= NUMBER_OF_QUEUES; i = i + interval) {
      int startQueueIndex = i;
      int endQueueIndex = i + interval - 1;
      if (endQueueIndex + interval - 1 > NUMBER_OF_QUEUES) {
        endQueueIndex = NUMBER_OF_QUEUES;
      }
      System.out.println("start thread:" + (thread_counter++) + " queue index from "
          + startQueueIndex + " to " + endQueueIndex);
      taskExecutor.execute(new BindQueueToExchangeWithRoutingKey("T" + thread_counter,
          startQueueIndex, endQueueIndex, startRoutingKeyIndex, endRoutingKeyIndex));
    }
  }

  private void createQueues() {
    for (int i = 1; i <= NUMBER_OF_QUEUES; i++) {
      System.out.println("Create queue:" + QUEUE_PREFIX + i);
      amqpAdmin.declareQueue(new Queue(QUEUE_PREFIX + i, true));
    }
  }

  // no harm if it's same
  private void createExchange() {
    rabbitManagementTemplate.addExchange(new DirectExchange(EXCHANGE_NAME, true, false));
  }

  private void sendMessageWithRandomRoutingKey() {
    int random_queue_number = new Random().nextInt(NUMBER_OF_QUEUES) + 1;
    int random_routingKey_number = new Random().nextInt(NUMBER_OF_ROUTING_KEYS) + 1;
    String routingKey = "rk-" + random_queue_number + "-" + random_routingKey_number;
    // System.out.println(routingKey);
    rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, generatePayload());
  }

  private Object generatePayload() {
    return "" + "helloworldhelloworld" + "helloworldhelloworld" + "helloworldhelloworld"
        + "helloworldhelloworld" + "helloworldhelloworld" + "helloworldhelloworld"
        + "helloworldhelloworld" + "helloworldhelloworld" + "helloworldhelloworld"
        + "helloworldhelloworld" + "helloworldhelloworldhelloworldhelloworld";
  }

  @Component
  @Scope("prototype")
  private class BindQueueToExchangeWithRoutingKey extends Thread {
    private String threadName = "";
    private int startQueueIndex = 0;
    private int endQueueIndex = -1;
    private int startRoutingKeyIndex = 0;
    private int endRoutingKeyIndex = -1;

    public BindQueueToExchangeWithRoutingKey(String threadName, int startQueueIndex,
        int endQueueIndex, int startRoutingKeyindex, int endRoutingKeyIndex) {
      this.threadName = threadName;
      this.startQueueIndex = startQueueIndex;
      this.endQueueIndex = endQueueIndex;
      this.startRoutingKeyIndex = startRoutingKeyindex;
      this.endRoutingKeyIndex = endRoutingKeyIndex;
    }

    @Override
    public void run() {
      for (int i = startQueueIndex; i <= endQueueIndex; i++) {
        for (int j = startRoutingKeyIndex; j <= endRoutingKeyIndex; j++) {
          amqpAdmin.declareBinding(new Binding("q" + i, DestinationType.QUEUE, EXCHANGE_NAME,
              "rk-" + i + "-" + j, new HashMap<String, Object>()));
          // System.out
          // .println("bind q" + i + " to " + exchange_name + " with " + "rk-" + i + "-" + j);
        }
      }
    }
  }
}
