package com.example.messagingrabbitmq.configs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.MimeTypeUtils;

import com.example.messagingrabbitmq.profiles.CloudReceiver;
import com.example.messagingrabbitmq.utilities.DummyRestaurants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("cloudReceiver")
public class CloudReceiverConfig {
  private final RMQPConnectionConfig rmqpConnectionConfig;

  @Autowired
  public CloudReceiverConfig(RMQPConnectionConfig rmqpConnectionConfig) {
    this.rmqpConnectionConfig = rmqpConnectionConfig;
  }

  @Bean
  public MessageConverter jsonToMapMessageConverter() {
    DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
    defaultClassMapper.setTrustedPackages("com.example.messagingrabbitmq.utilities");
    Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
    jackson2JsonMessageConverter.setClassMapper(defaultClassMapper);
    jackson2JsonMessageConverter.setSupportedContentType(MimeTypeUtils.APPLICATION_JSON);
    return jackson2JsonMessageConverter;
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
    cachingConnectionFactory.setHost(rmqpConnectionConfig.getHost());
    cachingConnectionFactory.setUsername(rmqpConnectionConfig.getUsername());
    cachingConnectionFactory.setPassword(rmqpConnectionConfig.getPassword());
    return cachingConnectionFactory;
  }

  @Bean
  public RabbitTemplate rabbitTemplate() {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
    rabbitTemplate.setMessageConverter(jsonToMapMessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public RabbitAdmin rabbitAdmin() {
    return new RabbitAdmin(rabbitTemplate());
  }

  public String stream() {
    return "up";
  }

  public TopicExchange topic(String restaurantId) {
    String exchangeName = "x." + restaurantId;
    return new TopicExchange(exchangeName);
  }

  public Queue queue(String restaurantId) {
    String queueName = "q." + restaurantId + "." + stream();
    return new Queue(queueName, true);
  }

  public String routingKey() {
    return "r.#";
  }

  @Bean
  public String queuesCsv() {
    List<String> queuesList = new ArrayList<>();
    for (Integer id : DummyRestaurants.entries.keySet()) {
      Queue queue = queue(id.toString());
      queuesList.add(queue.getName());
    }

    return String.join(",", queuesList);
  }

  @Bean
  public CloudReceiver cloudReceiver() {
    String routingKey = routingKey();
    for (Integer id : DummyRestaurants.entries.keySet()) {
      Queue queue = queue(id.toString());
      TopicExchange topic = topic(id.toString());
      rabbitAdmin().declareQueue(queue);
      Binding binding = BindingBuilder.bind(queue).to(topic).with(routingKey);
      rabbitAdmin().declareBinding(binding);
      log.info("Listening to '{}' QUEUE on '{}' EXCHANGE with '{}' as ROUTING KEY", queue.getName(), topic.getName(), routingKey);
    }
    return new CloudReceiver();
  }
}
