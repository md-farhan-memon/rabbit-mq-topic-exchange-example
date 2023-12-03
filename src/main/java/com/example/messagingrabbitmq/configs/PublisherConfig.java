package com.example.messagingrabbitmq.configs;

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

import com.example.messagingrabbitmq.profiles.Publisher;
import com.example.messagingrabbitmq.utilities.DummyRestaurants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile({"cloudPublisher", "localPublisher"})
public class PublisherConfig {
  @Value("${spring.profiles.active:Unknown}")
  private String activeProfile;

  @Value("${restaurantId:Unknown}")
  private String restaurantId;

  private final RMQPConnectionConfig rmqpConnectionConfig;

  @Autowired
  public PublisherConfig(RMQPConnectionConfig rmqpConnectionConfig) {
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
    return activeProfile.equals("cloudPublisher") ? "down" : "up";
  }

  @Bean
  public void initExchanges() {
    if (activeProfile.equals("cloudPublisher")) {
      for (Integer id : DummyRestaurants.entries.keySet()) {
        String exchangeName = "x." + id;
        rabbitAdmin().declareExchange(new TopicExchange(exchangeName));
        log.info("Exchanging On: {}", exchangeName);
      }
    } else {
      String exchangeName = "x." + restaurantId;
      rabbitAdmin().declareExchange(new TopicExchange(exchangeName));
      log.info("Exchanging On: {}", exchangeName);
    }
  }

  @Bean
  public Publisher publisher() {
    log.info("Publishing to {} Stream", stream());
    return new Publisher(rabbitTemplate(), stream());
  }
}
