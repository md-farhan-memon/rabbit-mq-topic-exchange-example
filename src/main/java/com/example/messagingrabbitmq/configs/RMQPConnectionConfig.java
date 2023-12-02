package com.example.messagingrabbitmq.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.Data;

@Data
@Primary
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RMQPConnectionConfig {
  private String host;
  private String port;
  private String username;
  private String password;
}
