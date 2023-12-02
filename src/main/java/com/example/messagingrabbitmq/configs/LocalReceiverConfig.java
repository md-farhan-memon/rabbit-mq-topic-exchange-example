package com.example.messagingrabbitmq.configs;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.MimeTypeUtils;

import com.example.messagingrabbitmq.profiles.LocalReceiver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("localReceiver")
public class LocalReceiverConfig {
  @Value("${restaurantId}")
  private String restaurantId;

  private final RMQPConnectionConfig rmqpConnectionConfig;

  @Autowired
  public LocalReceiverConfig(RMQPConnectionConfig rmqpConnectionConfig) {
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

  @Bean
  public String stream() {
    return "down";
  }

  @Bean
  public String restaurantId() {
    return restaurantId;
  }

  @Bean
  public TopicExchange topic() {
    String exchangeName = "x." + restaurantId();
    return new TopicExchange(exchangeName);
  }

  @Bean
  public Queue queue() {
    String queueName = "q." + restaurantId() + "." + stream();
    return new Queue(queueName, true);
  }

  @Bean
  public void initReceiverQueue() {
    rabbitAdmin().declareQueue(queue());
  }

  @Bean
  public String routingKey() {
    return "r.#";
  }

  @Bean
  public Binding binding(TopicExchange topic, Queue queue) {
    return BindingBuilder.bind(queue).to(topic).with(routingKey());
  }

  @Bean
  public LocalReceiver localReceiver() {
    log.info("Listening to '{}' QUEUE on '{}' EXCHANGE with '{}' as ROUTING KEY", queue().getName(), topic().getName(), routingKey());
    return new LocalReceiver();
  }
}
