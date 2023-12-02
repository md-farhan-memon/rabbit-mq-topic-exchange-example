package com.example.messagingrabbitmq.utilities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Validated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageBody implements Serializable {
  private UUID messageId = UUID.randomUUID();
  private Timestamp messageTimestamp = new Timestamp(System.currentTimeMillis());
  private String messageVersion = "1.0.0";
  private String restaurantId;
  private MessageType messageType;
  private Map<String, Serializable> payload;
}
