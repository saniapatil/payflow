package com.payflow.api.producer;
import com.payflow.api.config.RabbitMQConfig;
import com.payflow.api.dto.PaymentMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
@Service
public class PaymentProducer {
    private final RabbitTemplate rabbitTemplate;
    public PaymentProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendPayment(PaymentMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
    }
}
