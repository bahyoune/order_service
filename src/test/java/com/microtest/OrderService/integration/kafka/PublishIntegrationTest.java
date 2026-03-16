package com.microtest.OrderService.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microtest.OrderService.entity.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import com.microtest.OrderService.repository.OrderRepository;
import com.microtest.OrderService.kafka.KafkaPublisher;
import com.microtest.OrderService.support.TestEventFactory;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoConfigureMockMvc
//ActiveProfiles: test
public class PublishIntegrationTest extends ContainerKafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository repository;

    private Map<String, Object> props;

    @MockitoSpyBean
    private KafkaPublisher publisher;

    @BeforeEach
    void setup() {
        props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.microtest.event");

    }

    @Test
    public void test_createOrderWithSagaPattern() throws Exception {
        //GIVEN

        ConsumerFactory<String, OrderCreateEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props);

        Consumer<String, OrderCreateEvent> consumer = consumerFactory.createConsumer();

        consumer.subscribe(List.of("orders-topic3"));

        OrderCreateEvent request = TestEventFactory.orderCreateEvent_with_orderId();

        //WHEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/internal/v1/orders/saga")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", "ADMIN")
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        //THEN
        ConsumerRecords<String, OrderCreateEvent> records = consumer.poll(Duration.ofSeconds(10));

        org.assertj.core.api.Assertions.assertThat(records.count()).isEqualTo(1);

        Orders saved = repository.findById(1L).orElseThrow();
        Assertions.assertEquals(OrderStatus.CREATED, saved.getStatus());
    }

    @Test
    public void test_publishOrderInKafka_kafka_valid() throws Exception {
        //GIVEN
        ConsumerFactory<String, OrderEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props);

        Consumer<String, OrderEvent> consumer = consumerFactory.createConsumer();

        consumer.subscribe(List.of("orders-create"));


        OrderEvent request = TestEventFactory.orderEvent();

        //WHEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/internal/v1/orders/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", "ADMIN")
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        //THEN
        ConsumerRecords<String, OrderEvent> records = consumer.poll(Duration.ofSeconds(10));

        org.assertj.core.api.Assertions.assertThat(records.count()).isEqualTo(1);

    }

    @Test
    public void test_publishOrderInKafka_kafka_down() throws Exception {
        //GIVEN
        OrderEvent request = TestEventFactory.orderEvent();

        Mockito.doThrow(new RuntimeException("Kafka unavailable"))
                        .when(publisher)
                                .publishSimpleOrder( Mockito.any(OrderEvent.class), Mockito.anyString());

        //WHEN / THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/internal/v1/orders/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", "ADMIN")
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());


    }


}
