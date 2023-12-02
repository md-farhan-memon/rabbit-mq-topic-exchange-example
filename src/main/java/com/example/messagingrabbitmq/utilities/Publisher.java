package com.example.messagingrabbitmq.utilities;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class Publisher {

  private RabbitTemplate rabbitTemplate;
  private String stream;

  public void send(MessageBody messageBody) {
    String exchangeName = "x." + messageBody.getRestaurantId();
    String routingKey = "r." + messageBody.getMessageType().name() + "." + stream;
    rabbitTemplate.convertAndSend(exchangeName, routingKey, messageBody);
    log.info("> Message Published : {}", messageBody);
  }
}
