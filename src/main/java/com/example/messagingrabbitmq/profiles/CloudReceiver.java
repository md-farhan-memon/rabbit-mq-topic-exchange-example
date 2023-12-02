package com.example.messagingrabbitmq.profiles;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import com.example.messagingrabbitmq.utilities.MessageBody;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class CloudReceiver {
  @RabbitListener(queues = "#{queuesCsv.split(',')}")
  public void receive(MessageBody messageBody) {
    log.info("> Message Received : {}", messageBody);
  }
}
