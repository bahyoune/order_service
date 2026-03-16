package com.microtest.OrderService.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.microtest.OrderService.support.TestEventFactory;
import com.microtest.event.PaymentStatusEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@SpringBootTest(
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.openfeign.client.config.payment-service.url=http://localhost:8089"
        }
)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8089)
@ActiveProfiles("test")
public class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void test_role_user_status_401() throws Exception {
        //GIVEN

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/msg")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    public void test_role_user_status_403() throws Exception {
        //GIVEN
        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/msg")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("role", List.of("MANAGER"))
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    @Test
    public void test_role_user_status_200() throws Exception {
        //GIVEN

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/msg")
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
    public void test_findOrderForProductExist_not_validKey() throws Exception {
        //GIVEN
        String response = "Product checkout is out of stock";

        //Stub must match the exact feign path
        WireMock.stubFor(WireMock
                .get("/internal/v1/payment/checkout/availability")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("false")
                )
        );

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/feign/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", List.of("ADMIN"))
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(response));

    }

    @Test
    public void test_findOrderForProductExist_validKey() throws Exception {
        //GIVEN
        String response = "Order placed successfully for check";

        //Stub must match the exact feign path
        WireMock.stubFor(WireMock
                .get("/internal/v1/payment/check/availability")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")
                )
        );

        //WHEN /THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/feign/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", List.of("ADMIN"))
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(response));

    }

    @Test
    public void test_paymentStatus_service_available() throws Exception {
        //GIVEN
        PaymentStatusEvent result = TestEventFactory.paymentStatusEvent();


        //Stub must match the exact feign path
        WireMock.stubFor(WireMock
                .get("/internal/v1/payment/1")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(result))
                )
        );

        //WHEN
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/1/payment-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", List.of("ADMIN"))
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.request()
                        .asyncStarted())
                .andReturn();

        //THEN
        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("reason").value("SUCCESS"));
    }

    @Test
    public void test_paymentStatus_service_not_available() throws Exception {
        //GIVEN

        //Stub must match the exact feign path
        WireMock.stubFor(WireMock
                .get("/internal/v1/payment/1")
                .willReturn(WireMock.serverError())
        );

        //WHEN
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v1/orders/1/payment-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .claim("roles", List.of("ADMIN"))
                                        .subject("tester")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        )
                )
                .andExpect(MockMvcResultMatchers.request()
                        .asyncStarted())
                .andReturn();

        //THEN
        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("reason").value("Payment Service not available"));
    }


}
