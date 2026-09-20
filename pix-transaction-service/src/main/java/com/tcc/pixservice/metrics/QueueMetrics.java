package com.tcc.pixservice.metrics;

import com.tcc.pixservice.config.RabbitMQConfig;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QueueMetrics {

    public QueueMetrics(MeterRegistry meterRegistry, AmqpAdmin amqpAdmin) {
        registrar(meterRegistry, amqpAdmin, RabbitMQConfig.PIX_TRANSACTION_PROCESSED_QUEUE);
        registrar(meterRegistry, amqpAdmin, RabbitMQConfig.PIX_TRANSACTION_PROCESSED_DLQ);
    }

    private void registrar(MeterRegistry meterRegistry, AmqpAdmin amqpAdmin, String fila) {
        Gauge.builder("pix.queue.messages", amqpAdmin, admin -> contarMensagens(admin, fila))
                .description("Mensagens aguardando na fila do RabbitMQ")
                .tag("queue", fila)
                .strongReference(true)
                .register(meterRegistry);
    }

    private double contarMensagens(AmqpAdmin amqpAdmin, String fila) {
        try {
            QueueInformation info = amqpAdmin.getQueueInfo(fila);
            if (info == null) {
                return Double.NaN;
            }
            return info.getMessageCount();
        } catch (Exception e) {
            log.debug("Não foi possível consultar a fila {}: {}", fila, e.getMessage());
            return Double.NaN;
        }
    }
}
