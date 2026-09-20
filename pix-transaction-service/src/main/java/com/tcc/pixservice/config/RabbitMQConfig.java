package com.tcc.pixservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PIX_TRANSACTION_EXCHANGE =
            "pix.transaction.exchange";

    public static final String PIX_TRANSACTION_REQUESTED_ROUTING_KEY =
            "pix.transaction.requested";

    public static final String PIX_TRANSACTION_PROCESSED_ROUTING_KEY =
            "pix.transaction.processed";

    public static final String PIX_TRANSACTION_PROCESSED_QUEUE =
            "pix.transaction.processed.queue";

    public static final String PIX_TRANSACTION_PROCESSED_DLX =
            "pix.transaction.processed.dlx";

    public static final String PIX_TRANSACTION_PROCESSED_DLQ =
            "pix.transaction.processed.dlq";

    @Bean
    public TopicExchange pixTransactionExchange() {
        return new TopicExchange(PIX_TRANSACTION_EXCHANGE, true, false);
    }

    @Bean
    public org.springframework.amqp.core.DirectExchange pixTransactionProcessedDlx() {
        return new org.springframework.amqp.core.DirectExchange(PIX_TRANSACTION_PROCESSED_DLX, true, false);
    }

    @Bean
    public Queue pixTransactionProcessedQueue() {
        return QueueBuilder.durable(PIX_TRANSACTION_PROCESSED_QUEUE)
                .withArgument("x-dead-letter-exchange", PIX_TRANSACTION_PROCESSED_DLX)
                .withArgument("x-dead-letter-routing-key", PIX_TRANSACTION_PROCESSED_DLQ)
                .build();
    }

    @Bean
    public Queue pixTransactionProcessedDlq() {
        return QueueBuilder.durable(PIX_TRANSACTION_PROCESSED_DLQ).build();
    }

    @Bean
    public Binding pixTransactionProcessedBinding(
            Queue pixTransactionProcessedQueue,
            TopicExchange pixTransactionExchange
    ) {
        return BindingBuilder
                .bind(pixTransactionProcessedQueue)
                .to(pixTransactionExchange)
                .with(PIX_TRANSACTION_PROCESSED_ROUTING_KEY);
    }

    @Bean
    public Binding pixTransactionProcessedDlqBinding(
            Queue pixTransactionProcessedDlq,
            org.springframework.amqp.core.DirectExchange pixTransactionProcessedDlx
    ) {
        return BindingBuilder
                .bind(pixTransactionProcessedDlq)
                .to(pixTransactionProcessedDlx)
                .with(PIX_TRANSACTION_PROCESSED_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter converter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);

        // propaga o contexto do trace (traceparent) nos headers das mensagens
        template.setObservationEnabled(true);
        return template;
    }
}
