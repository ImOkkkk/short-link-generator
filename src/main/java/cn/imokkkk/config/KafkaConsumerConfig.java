package cn.imokkkk.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

/**
 * @author ImOkkkk
 * @date 2022/4/27 13:25
 * @since 1.0
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Value("${kafka.bootstrap-servers:127.0.0.1:9092}")
  private String brokerAddress;

  @Value("${kafka.consumer.maxPollSize:1000}")
  private int maxPollSize;

  @Value("${kafka.consumer.groupId:shortURL_storage}")
  private String groupId;

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollSize);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConcurrency(3);
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

  public Consumer<String, String> getConsumer(List<String> topics) {
    ConsumerFactory<String, String> consumerFactory = consumerFactory();
    Consumer<String, String> consumer = consumerFactory.createConsumer();
    consumer.subscribe(topics);
    return consumer;
  }
}
