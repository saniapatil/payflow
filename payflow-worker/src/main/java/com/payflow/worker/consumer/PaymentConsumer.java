package com.payflow.worker.consumer;
import com.payflow.worker.config.RabbitMQConfig;
import com.payflow.worker.dto.PaymentMessage;
import com.payflow.worker.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
@Service
public class PaymentConsumer {
    private final PaymentService paymentService;
    public PaymentConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processPayment(PaymentMessage paymentMessage) {
        paymentService.processPayment(paymentMessage);
    }
}
