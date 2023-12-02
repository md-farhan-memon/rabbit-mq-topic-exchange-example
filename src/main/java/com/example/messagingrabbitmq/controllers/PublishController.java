package com.example.messagingrabbitmq.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.messagingrabbitmq.configs.PublisherConfig;
import com.example.messagingrabbitmq.utilities.MessageBody;
import com.example.messagingrabbitmq.utilities.MessageType;
import com.example.messagingrabbitmq.utilities.Publisher;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;

@RestController
@Profile({"cloudPublisher", "localPublisher"})
@RequestMapping(
  value = "/api/publish",
  produces = MediaType.APPLICATION_JSON_VALUE,
  consumes = MediaType.APPLICATION_JSON_VALUE
)
public class PublishController {
  private final Publisher publisher;

  @Autowired
  public PublishController(PublisherConfig publisherConfig) {
    this.publisher = publisherConfig.publisher();
  }

  @PostMapping("/message")
  public ResponseEntity<JsonNode> message(@RequestBody @Valid MessageBody messageBody) {
    messageBody.setMessageType(MessageType.MESSAGE_TYPE_1);
    publisher.send(messageBody);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
