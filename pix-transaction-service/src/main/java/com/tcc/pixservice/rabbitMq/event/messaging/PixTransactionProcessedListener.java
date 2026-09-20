package com.tcc.pixservice.rabbitMq.event.messaging;

import com.tcc.pixservice.config.RabbitMQConfig;
import com.tcc.pixservice.rabbitMq.event.PixTransactionProcessedEvent;
import com.tcc.pixservice.service.PixTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PixTransactionProcessedListener {

    private final PixTransactionService pixTransactionService;

    @RabbitListener(queues = RabbitMQConfig.PIX_TRANSACTION_PROCESSED_QUEUE)
    public void processar(PixTransactionProcessedEvent event) {

        log.info("Resultado de transação Pix recebido. transactionId={} status={}",
                event.getTransactionId(), event.getStatus());

        try {

            pixTransactionService.atualizarResultado(event);

        } catch (Exception e) {

            // Mesmo raciocínio do PixTransactionListener no account-service:
            // logamos e relançamos de propósito, deixando o mecanismo de
            // retry (application.yaml) e a dead-letter queue lidarem com
            // falhas inesperadas, em vez de engolir o erro silenciosamente.
            log.error("Erro inesperado ao processar resultado da transação Pix. transactionId={}",
                    event.getTransactionId(), e);

            throw e;
        }
    }
}
