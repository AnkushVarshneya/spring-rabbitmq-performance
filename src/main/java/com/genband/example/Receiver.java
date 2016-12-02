package com.genband.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

// @Component
public class Receiver {
  private Logger logger = LoggerFactory.getLogger(Receiver.class);

  // it supports class-level dispatcher as well
  @RabbitListener(containerFactory = "simpleRabbitListenerContainerFactory", queues = "q1")
  public void receiveMessage(Message message) {
    System.out.println(message.getBody());
  }
}
