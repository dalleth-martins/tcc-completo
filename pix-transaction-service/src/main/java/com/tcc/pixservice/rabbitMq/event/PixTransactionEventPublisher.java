package com.tcc.pixservice.rabbitMq.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.tcc.pixservice.config.RabbitMQConfig.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PixTransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPixTransactionRequested(PixTransactionRequestedEvent event) {

        log.info("Publicando solicitação de transação Pix. transactionId={} sourceAccountId={} destinationAccountId={}",
                event.getTransactionId(), event.getSourceAccountId(), event.getDestinationAccountId());

        rabbitTemplate.convertAndSend(
                PIX_TRANSACTION_EXCHANGE,
                PIX_TRANSACTION_REQUESTED_ROUTING_KEY,
                event
        );
    }
}
