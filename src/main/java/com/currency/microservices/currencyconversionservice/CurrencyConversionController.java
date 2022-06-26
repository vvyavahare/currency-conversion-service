package com.currency.microservices.currencyconversionservice;

import java.math.BigDecimal;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class CurrencyConversionController {

  @Value(value = "${kafka.topic.currency.conversion}")
  private String currencyConversionTopic;

  @Autowired
  private CurrencyExchangeProxy proxy;

  @Autowired
  private KafkaTemplate kafkaTemplate;

  @Autowired
  private ObjectMapper mapper;

  @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
  public CurrencyConversion calculateCurrencyConversion(
      @PathVariable String from,
      @PathVariable String to,
      @PathVariable BigDecimal quantity
  ) {

    HashMap<String, String> uriVariables = new HashMap<>();
    uriVariables.put("from", from);
    uriVariables.put("to", to);

    ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity
        ("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
         CurrencyConversion.class, uriVariables);

    CurrencyConversion currencyConversion = responseEntity.getBody();

    return new CurrencyConversion(currencyConversion.getId(),
                                  from, to, quantity,
                                  currencyConversion.getConversionMultiple(),
                                  quantity.multiply(currencyConversion.getConversionMultiple()),
                                  currencyConversion.getEnvironment() + " " + "rest template");

  }

  @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
  public CurrencyConversion calculateCurrencyConversionFeign(
      @PathVariable String from,
      @PathVariable String to,
      @PathVariable BigDecimal quantity
  ) {

    CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);

    try {
      kafkaTemplate.send(currencyConversionTopic, mapper.writeValueAsString(currencyConversion));
      log.info("Sent data to kafka successfully...");
    } catch (Exception e) {
      log.error("Error in sending data to kafka.", e);
    }

    return new CurrencyConversion(currencyConversion.getId(),
                                  from, to, quantity,
                                  currencyConversion.getConversionMultiple(),
                                  quantity.multiply(currencyConversion.getConversionMultiple()),
                                  currencyConversion.getEnvironment() + " " + "feign");

  }


}
