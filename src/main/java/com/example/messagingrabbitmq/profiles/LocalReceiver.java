package com.example.messagingrabbitmq.profiles;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import com.example.messagingrabbitmq.utilities.MessageBody;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalReceiver {
  @RabbitListener(queues = "#{'q.' + ${restaurantId} + '.down'}")
  public void receive(MessageBody messageBody) {
    log.info("> Message Received : {}", messageBody);
  }
}
