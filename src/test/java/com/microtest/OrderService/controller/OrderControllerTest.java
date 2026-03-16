package com.microtest.OrderService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microtest.OrderService.exception.KafkaErrorPublishException;
import com.microtest.OrderService.service.OrderService;
import com.microtest.OrderService.service.OrderFeignService;
import com.microtest.OrderService.support.TestEventFactory;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@WebMvcTest(
        properties = "spring.cloud.config.enabled=false"
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderFeignService feignService;

    @Test
    public void test_message() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/msg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("role", List.of("ADMIN"))
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void test_findOrderForProductExist() throws Exception {
        //GIVEN
        String productId = "1";

        String request = "Order placed successfully for " + productId;

        Mockito.when(feignService.findOrderForProductExist(Mockito.anyString()))
                .thenReturn(request);

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/feign/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("role", List.of("ADMIN"))
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(feignService, Mockito.timeout(1))
                .findOrderForProductExist(Mockito.anyString());
    }

    @Test
    public void test_paymentStatus_fetch_success() throws Exception {
        //GIVEN
        Long orderId = 1L;

        PaymentStatusEvent result = TestEventFactory.paymentStatusEvent();

        Mockito.when(feignService.getPaymentStatus(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(result));

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders.get("/internal/v1/orders/{orderId}/payment-status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("role", "ADMIN")
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(feignService, Mockito.timeout(1))
                .getPaymentStatus(Mockito.anyLong());
    }


    @Test
    public void test_publishOrderInKafka_kafka_up() throws Exception {
        //GIVEN
        OrderEvent request = TestEventFactory.orderEvent_request();

        PaymentStatusEvent result = TestEventFactory.paymentStatusEvent();

        Mockito.when(orderService.publishOrderInKafka(Mockito.any(OrderEvent.class)))
                .thenReturn(result);

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders.post("/internal/v1/orders/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                        .with(
                                SecurityMockMvcRequestPostProcessors.jwt()
                                        .jwt(jwt -> jwt
                                                .claim("roles", List.of("ADMIN"))
                                                .subject("tester")
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("OrderId").exists());

        Mockito.verify(orderService, Mockito.timeout(1))
                .publishOrderInKafka(Mockito.any(OrderEvent.class));

    }

    @Test
    public void test_publishOrderInKafka_kafka_down() throws Exception {
        //GIVEN
        OrderEvent request = TestEventFactory.orderEvent_request();


        Mockito.doThrow(KafkaErrorPublishException.class)
                .when(orderService)
                .publishOrderInKafka(Mockito.any(OrderEvent.class));

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders.post("/internal/v1/orders/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                        .with(
                                SecurityMockMvcRequestPostProcessors.jwt()
                                        .jwt(jwt -> jwt
                                                .claim("roles", List.of("ADMIN"))
                                                .subject("tester")
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(orderService, Mockito.timeout(1))
                .publishOrderInKafka(Mockito.any(OrderEvent.class));

    }

    @Test
    public void test_createOrderWithSagaPattern_kafka_up() throws Exception {
        //GIVEN
        OrderCreateEvent request = TestEventFactory.orderCreateEvent();

        PaymentStatusEvent result = TestEventFactory.paymentStatusEvent();

        Mockito.when(orderService.createOrderWithSagaPattern(Mockito.any(OrderCreateEvent.class)))
                .thenReturn(result);

        //WHEN / THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/internal/v1/orders/saga")
                        .with(
                                SecurityMockMvcRequestPostProcessors.jwt()
                                        .jwt(jwt -> jwt
                                                .claim("roles", List.of("ADMIN"))
                                                .subject("tester")
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))

                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("OrderId").exists());

        Mockito.verify(orderService, Mockito.timeout(1))
                .createOrderWithSagaPattern(Mockito.any(OrderCreateEvent.class));
    }

    @Test
    public void test_createOrderWithSagaPattern_kafka_down() throws Exception {
        //GIVEN
        OrderCreateEvent request = TestEventFactory.orderCreateEvent();

        Mockito.doThrow(KafkaErrorPublishException.class)
                .when(orderService)
                .createOrderWithSagaPattern(Mockito.any(OrderCreateEvent.class));

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders.post("/internal/v1/orders/saga")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", List.of("ADMIN"))
                                        .subject("teste")

                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(orderService, Mockito.timeout(1))
                .createOrderWithSagaPattern(Mockito.any(OrderCreateEvent.class));
    }


}
