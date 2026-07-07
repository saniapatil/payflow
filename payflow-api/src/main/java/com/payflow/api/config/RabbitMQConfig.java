package com.payflow.api.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "paymentQueue";
    public static final String DLQ = "paymentDLQ";
    public static final String EXCHANGE = "paymentExchange";
    public static final String ROUTING_KEY = "paymentRoutingKey";
    public static final String DLQ_ROUTING_KEY = "paymentDLQRoutingKey";
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }
    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_NAME).withArgument("x-dead-letter-exchange", EXCHANGE).withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY).build();
    }
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ);
    }
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
    }
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(exchange()).with(DLQ_ROUTING_KEY);
    }
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
