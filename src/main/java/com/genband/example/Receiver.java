package com.genband.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {
  private Logger logger = LoggerFactory.getLogger(Receiver.class);

  // it supports class-level dispatcher as well
  // @RabbitListener(containerFactory = "simpleRabbitListenerContainerFactory", queues = "q1")
  public void receiveMessage(Message message) {
    // logger.info("received Message<" + message + ">");
    System.out.println(message.getBody());
  }

  // @RabbitListener(bindings = @QueueBinding(
  // value = @Queue(value = RabbitMQConfiguration.ALTERNATIVE_QUEUE_NAME, autoDelete = "true"),
  // exchange = @Exchange(value = RabbitMQConfiguration.ALTERNATIVE_EXCHANGE_NAME,
  // autoDelete = "true"),
  // key = ""))
  // public String handleWithDeclare(String foo) {
  // return foo.toUpperCase();
  // }
}
