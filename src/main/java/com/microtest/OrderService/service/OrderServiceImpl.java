package com.microtest.OrderService.service;

import com.microtest.OrderService.bean.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import com.microtest.OrderService.repo.OrderRepository;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderRepository orderRepository;

    @Value("${app.kafka.topic.order}")
    private String topic;
    @Value("${app.kafka.topic.ordercreate}")
    private String topic1;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public OrderServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    //<editor-fold defaultState="collapsed" desc="Change, Forgot Password">


    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Test simple for Kafka event">
    //if pass, send success
    //else send DLT error message
    public void sendOrder(OrderEvent event) {
//        kafkaTemplate.send
        kafkaTemplate.send(topic, event.getOrderId(), event)
                .whenComplete((result, ex) ->{
                    if(ex == null) {
                        System.out.println("📤 Sent: " + event);
                    }else {
                        System.err.println("❌ Failed to send event: " + ex.getMessage());
                    }
                });


    }
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Saga Pattern of Kafka">
    public Orders createOrder(String userId, double amount) {
        Orders order = new Orders();
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus(OrderStatus.CREATED);

        orderRepository.save(order);

        kafkaTemplate.send(topic1, order.getId().toString(), new OrderCreateEvent(order.getId(), userId,amount));

        return order;
    }
    //</editor-fold>


}
