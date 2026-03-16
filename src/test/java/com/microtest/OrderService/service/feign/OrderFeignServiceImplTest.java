package com.microtest.OrderService.service.feign;

import com.microtest.OrderService.feign.PaymentFeign;
import com.microtest.OrderService.service.impl.OrderFeignServiceImpl;
import com.microtest.OrderService.support.TestEventFactory;
import com.microtest.event.PaymentStatusEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;


@ExtendWith(MockitoExtension.class)
public class OrderFeignServiceImplTest {

    @Mock
    private PaymentFeign paymentFeign;

    @InjectMocks
    private OrderFeignServiceImpl orderFeignService;


    @Test
    public void test_getPaymentStatus_service_valid() {
        //GIVEN
        PaymentStatusEvent event = TestEventFactory.paymentStatusEvent();

        Mockito.when(paymentFeign.getPaymentStatus(Mockito.anyLong()))
                .thenReturn(event);

        //WHEN
        var result = paymentFeign.getPaymentStatus(1L);

        //THEN
        Assertions.assertNotNull(result);
        Mockito.verify(paymentFeign, Mockito.timeout(1))
                .getPaymentStatus(Mockito.anyLong());

    }

    @Test
    public void test_getPaymentStatus_service_down() throws Exception{
        //GIVEN
        Long orderId = 1L;


        //WHEN
        var result =orderFeignService.paymentStatusFallback(orderId, new RuntimeException("Service Down"));

        //THEN
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Payment Service not available",result.get().reason());

    }

    @Test
    public void test_findOrderForProductExist_product_exist() {
        //GIVEN
        String productId = "1";
        Mockito.when(paymentFeign.isIdPaymentExist(Mockito.anyString()))
                .thenReturn(new ResponseEntity<>(true, HttpStatusCode.valueOf(200)));

        //WHEN
        var result = orderFeignService.findOrderForProductExist(productId);

        //THEN
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Order placed successfully for " + productId, result);
    }

    @Test
    public void test_findOrderForProductExist_product_notExist() {
        //GIVEN
        String productId = "1";
        Mockito.when(paymentFeign.isIdPaymentExist(Mockito.anyString()))
                .thenReturn(new ResponseEntity<>(false, HttpStatusCode.valueOf(200)));

        //WHEN
        var result = orderFeignService.findOrderForProductExist(productId);

        //THEN
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Product " + productId + " is out of stock", result);
    }

}
