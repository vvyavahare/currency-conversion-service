package com.currency.microservices.currencyconversionservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {


  @Value(value = "${kafka.bootstrap.server}")
  private String kafkaBootstrapServer;

  @Value(value = "${kafka.topic.currency.conversion}")
  private String currencyConversionTopic;

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic createCurrencyConversionTopic() {
    return new NewTopic(currencyConversionTopic, 1, (short) 1);
  }
}